package de.dogcraft.ssltest.tests;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;

import org.bouncycastle.crypto.tls.BugTestingTLSClient;
import org.bouncycastle.crypto.tls.CompressionMethod;
import org.bouncycastle.crypto.tls.HeartbeatMessage;
import org.bouncycastle.crypto.tls.HeartbeatMessageType;

import de.dogcraft.ssltest.utils.CipherProbingClient;

public class TestImplementationBugs {

    private org.bouncycastle.crypto.tls.Certificate cert;

    private final String host;

    private final int port;

    public TestImplementationBugs(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean testDeflate(TestOutput pw) throws IOException {
        Socket sock = new Socket(host, port);
        TestingTLSClient tcp = new TestingTLSClient(sock.getInputStream(), sock.getOutputStream());
        CipherProbingClient tc = new CipherProbingClient(host, port, TestCipherList.getAllCiphers(), new short[] { CompressionMethod.DEFLATE });
        try {
            tcp.connect(tc);
            sock.getOutputStream().flush();
            tcp.close();
            sock.close();
        } catch (Throwable t) {

        }
        return tcp.hasFailedLocaly() || tc.isFailed();
    }

    public void testBug(TestOutput pw) throws IOException {
        Socket sock = new Socket(host, port);
        BugTestingTLSClient tcp = new BugTestingTLSClient(this, sock.getInputStream(), sock.getOutputStream());
        CipherProbingClient tc = new CipherProbingClient(host, port, TestCipherList.getAllCiphers(), new short[] { CompressionMethod._null });
        tcp.connect(tc);
        HeartbeatMessage hbm = new HeartbeatMessage(HeartbeatMessageType.heartbeat_request, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 }, 16);
        sock.setSoTimeout(1500);
        boolean hb = false;
        try {
            hb = tcp.sendHeartbeat(hbm, false);
        } catch (IOException e) {
        }
        boolean resp = tcp.fetchRecievedHB();
        boolean bleed = false;
        try {
            bleed = tcp.sendHeartbeat(hbm, true);
        } catch (IOException e) {
        }
        boolean resp2 = tcp.fetchRecievedHB();

        if (hb && resp) {
            pw.output("heartbeat works");
        } else {
            pw.output("heartbeat works not");
        }
        if (bleed && resp2) {
            pw.output("heartbleed works!!!");
        } else {
            pw.output("heartbleed works not");
        }
        try {
            sock.getOutputStream().flush();
            tcp.close();
            sock.close();
        } catch (Throwable t) {

        }
    }

    private Hashtable extensions;

    private String cipherInfo;

    public org.bouncycastle.crypto.tls.Certificate getCert() {
        return cert;
    }

    public Hashtable getExt() {
        return extensions;
    }

    public void setCiperInfo(String cipherInfo) {
        this.cipherInfo = cipherInfo;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getCipherInfo() {
        return cipherInfo;
    }

}