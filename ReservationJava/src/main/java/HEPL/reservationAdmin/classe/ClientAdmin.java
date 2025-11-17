package HEPL.reservationAdmin.classe;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ClientAdmin {
    private final String host;
    private final int port;
    private final int ctoMs;
    private final int rtoMs;

    public ClientAdmin(String host, int port, int ctoMs, int rtoMs) {
        this.host = host;
        this.port = port;
        this.ctoMs = ctoMs;
        this.rtoMs = rtoMs;
    }

    public String call(String frame) throws IOException {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), ctoMs);
            s.setSoTimeout(rtoMs);

            OutputStream out = s.getOutputStream();
            out.write(frame.getBytes(StandardCharsets.UTF_8));
            out.flush();

            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            InputStream in = s.getInputStream();
            byte[] tmp = new byte[2048];
            for (;;) {
                int n = in.read(tmp);
                if (n < 0) break; // serveur ferme => fin de message
                buf.write(tmp, 0, n);
            }
            return buf.toString(StandardCharsets.UTF_8);
        }
    }

    public List<ClientInfo> parseAllClient(String resp) {
        String[] parts = resp.split("#", 3);
        if (parts.length < 2) return List.of();
        String status = parts[1];
        if (!"ok".equalsIgnoreCase(status)) return List.of();

        String payload = parts.length == 3 ? parts[2] : "";
        if (payload.isEmpty()) return List.of();

        List<ClientInfo> out = new ArrayList<>();
        for (String bloc : payload.split("\\|")) {
            if (bloc.isEmpty()) continue;
            String[] f = bloc.split(";", -1); // ip;nom;prenom;no
            if (f.length < 4) continue;
            out.add(new ClientInfo(f[0], f[1], f[2], f[3]));
        }
        return out;
    }

    public static final class ClientInfo {
        public final String ip, nom, prenom, no;
        public ClientInfo(String ip, String nom, String prenom, String no) {
            this.ip = ip; this.nom = nom; this.prenom = prenom; this.no = no;
        }
        @Override public String toString() { return ip + " " + nom + " " + prenom + " " + no; }
    }
}
