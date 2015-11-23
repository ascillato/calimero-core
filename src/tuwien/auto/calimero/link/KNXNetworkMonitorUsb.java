/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2015 B. Malinowsky

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    Linking this library statically or dynamically with other modules is
    making a combined work based on this library. Thus, the terms and
    conditions of the GNU General Public License cover the whole
    combination.

    As a special exception, the copyright holders of this library give you
    permission to link this library with independent modules to produce an
    executable, regardless of the license terms of these independent
    modules, and to copy and distribute the resulting executable under terms
    of your choice, provided that you also meet, for each linked independent
    module, the terms and conditions of the license of that module. An
    independent module is a module which is not derived from or based on
    this library. If you modify this library, you may extend this exception
    to your version of the library, but you are not obligated to do so. If
    you do not wish to do so, delete this exception statement from your
    version.
*/

package tuwien.auto.calimero.link;

import java.util.EnumSet;

import tuwien.auto.calimero.DeviceDescriptor;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.KNXFormatException;
import tuwien.auto.calimero.KNXTimeoutException;
import tuwien.auto.calimero.cemi.CEMI;
import tuwien.auto.calimero.cemi.CEMIDevMgmt;
import tuwien.auto.calimero.knxnetip.KNXConnectionClosedException;
import tuwien.auto.calimero.link.BcuSwitcher.BcuMode;
import tuwien.auto.calimero.link.medium.KNXMediumSettings;
import tuwien.auto.calimero.link.medium.PLSettings;
import tuwien.auto.calimero.serial.KNXPortClosedException;
import tuwien.auto.calimero.serial.usb.HidReport;
import tuwien.auto.calimero.serial.usb.TransferProtocolHeader.KnxTunnelEmi;
import tuwien.auto.calimero.serial.usb.UsbConnection;
import tuwien.auto.calimero.serial.usb.UsbConnection.EmiType;

/**
 * Implementation of the KNX network monitor link over USB, using a {@link UsbConnection}. Once a
 * monitor has been closed, it is not available for further link communication, i.e., it can't be
 * reopened.
 *
 * @author B. Malinowsky
 */
// XXX cEMI link layer monitor not tested
public class KNXNetworkMonitorUsb extends AbstractMonitor
{
	private static final int PEI_SWITCH = 0xA9;

	private final UsbConnection conn;
	private final EnumSet<EmiType> emiTypes;
	private EmiType activeEmi;

	/**
	 * Creates a new network monitor for accessing the KNX network over a USB connection.
	 * <p>
	 *
	 * @param settings medium settings defining the specific KNX medium needed for decoding raw
	 *        frames received from the KNX network
	 * @throws KNXException
	 */
	public KNXNetworkMonitorUsb(final int vendorId, final int productId,
		final KNXMediumSettings settings) throws KNXException, InterruptedException
	{
		this(new UsbConnection(vendorId, productId), settings);
	}

	/**
	 * Creates a new network monitor for accessing the KNX network over a USB connection.
	 * <p>
	 *
	 * @param settings medium settings defining the specific KNX medium needed for decoding raw
	 *        frames received from the KNX network
	 * @throws KNXException
	 */
	public KNXNetworkMonitorUsb(final String device, final KNXMediumSettings settings)
		throws KNXException, InterruptedException
	{
		this(new UsbConnection(device), settings);
	}

	protected KNXNetworkMonitorUsb(final UsbConnection c, final KNXMediumSettings settings)
		throws KNXException, InterruptedException
	{
		super(c, c.getName(), settings);
		conn = c;
		try {
			if (!conn.isKnxConnectionActive())
				throw new KNXConnectionClosedException("interface is not connected to KNX network");
			emiTypes = conn.getSupportedEmiTypes();
			if (!trySetActiveEmi(EmiType.CEmi) && !trySetActiveEmi(EmiType.Emi2)
					&& !trySetActiveEmi(EmiType.Emi1)) {
				throw new KNXConnectionClosedException("failed to set active any supported EMI type");
			}
		}
		catch (final KNXException e) {
			close();
			throw e;
		}

		final boolean extBusmon = settings instanceof PLSettings;
		enterBusmonitor(extBusmon);
		try {
			final int dd0 = conn.getDeviceDescriptorType0();
			logger.info("Device Descriptor {}", DeviceDescriptor.DD0.fromType0(dd0));
		}
		catch (final KNXTimeoutException e) {
			// device does not provide a device descriptor 0
		}

		logger.info("in busmonitor mode - ready to receive");
		conn.addConnectionListener(notifier);
	}

	private boolean trySetActiveEmi(final EmiType active)
		throws KNXPortClosedException, KNXTimeoutException, InterruptedException
	{
		if (emiTypes.contains(active)) {
			conn.setActiveEmiType(active);
			activeEmi = conn.getActiveEmiType();
			return activeEmi == active;
		}
		return false;
	}

	private void enterBusmonitor(final boolean extBusmon)
		throws KNXPortClosedException, KNXTimeoutException, KNXFormatException, InterruptedException
	{
		if (activeEmi == EmiType.CEmi) {
			final int CEMI_SERVER_OBJECT = 8;
			final int PID_COMM_MODE = 52;
			final int objectInstance = 1;
			final CEMI frame = new CEMIDevMgmt(CEMIDevMgmt.MC_PROPWRITE_REQ, CEMI_SERVER_OBJECT,
					objectInstance, PID_COMM_MODE, 1, 1, new byte[] { 1 });
			conn.send(HidReport.create(KnxTunnelEmi.CEmi, frame.toByteArray()).get(0), true);
			// TODO close monitor if we cannot switch to busmonitor
			// check for .con
			//findFrame(CEMIDevMgmt.MC_PROPWRITE_CON);
		}
		else if (activeEmi == EmiType.Emi1) {
			new BcuSwitcher(conn, logger)
					.enter(extBusmon ? BcuMode.ExtBusmonitor : BcuMode.Busmonitor);
		}
		else {
			final byte[] switchBusmon = { (byte) PEI_SWITCH, (byte) 0x90, 0x18, 0x34, 0x56, 0x78,
				0x0A, };
			conn.send(HidReport.create(activeEmi.emi, switchBusmon).get(0), true);
		}
	}

	protected void leaveBusmonitor() throws InterruptedException
	{
		try {
			normalMode();
		}
		catch (final KNXPortClosedException | KNXTimeoutException e) {}
	}

	private void normalMode()
		throws KNXPortClosedException, KNXTimeoutException, InterruptedException
	{
		if (activeEmi == EmiType.CEmi) {
			final CEMI frame = new CEMIDevMgmt(CEMIDevMgmt.MC_RESET_REQ);
			conn.send(HidReport.create(KnxTunnelEmi.CEmi, frame.toByteArray()).get(0), true);
		}
		else if (activeEmi == EmiType.Emi1) {
			new BcuSwitcher(conn, logger).reset();
		}
		else {
			final byte[] switchNormal = { (byte) PEI_SWITCH, 0x1E, 0x12, 0x34, 0x56, 0x78,
				(byte) 0x9A, };
			conn.send(HidReport.create(activeEmi.emi, switchNormal).get(0), true);
		}
	}
}
