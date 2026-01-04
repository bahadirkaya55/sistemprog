package com.example.family;

/**
 * Uygulama giriş noktası.
 */
public class NodeMain {

    public static void main(String[] args) {
        if (args.length == 0) {
            // GUI modunu başlat
            javax.swing.SwingUtilities.invokeLater(() -> {
                new HaToKuSeGUI().setVisible(true);
            });
            return;
        }

        String role = args[0].toLowerCase();

        switch (role) {
            case "leader":
                DiskStorage.WriteMode leaderMode = parseWriteMode(args.length > 1 ? args[1] : null);
                LeaderNode leader = new LeaderNode(leaderMode);
                leader.start();
                break;

            case "member":
                int port = 7000;
                DiskStorage.WriteMode memberMode = DiskStorage.WriteMode.BUFFERED;
                String leaderHost = "localhost";
                int leaderPort = 6000;

                if (args.length > 1) {
                    try {
                        port = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                    }
                }
                if (args.length > 2) {
                    memberMode = parseWriteMode(args[2]);
                }
                if (args.length > 3) {
                    leaderHost = args[3];
                }
                if (args.length > 4) {
                    try {
                        leaderPort = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                    }
                }

                MemberNode member = new MemberNode(port, leaderHost, leaderPort, memberMode);
                member.start();
                break;

            case "client":
                String[] clientArgs = new String[args.length - 1];
                System.arraycopy(args, 1, clientArgs, 0, args.length - 1);
                HaToKuSeClient.main(clientArgs);
                break;

            case "gui":
                javax.swing.SwingUtilities.invokeLater(() -> {
                    new HaToKuSeGUI().setVisible(true);
                });
                break;

            default:
                System.err.println("Bilinmeyen rol: " + role);
                printUsage();
        }
    }

    private static DiskStorage.WriteMode parseWriteMode(String mode) {
        if (mode == null)
            return DiskStorage.WriteMode.BUFFERED;
        switch (mode.toLowerCase()) {
            case "buffered":
                return DiskStorage.WriteMode.BUFFERED;
            case "unbuffered":
                return DiskStorage.WriteMode.UNBUFFERED;
            case "zerocopy":
                return DiskStorage.WriteMode.ZERO_COPY;
            case "memorymapped":
                return DiskStorage.WriteMode.MEMORY_MAPPED;
            default:
                return DiskStorage.WriteMode.BUFFERED;
        }
    }

    private static void printUsage() {
        System.out.println("Kullanım:");
        System.out.println("  java -jar app.jar                    -> GUI başlat");
        System.out.println("  java -jar app.jar gui                -> GUI başlat");
        System.out.println("  java -jar app.jar leader [mode]");
        System.out.println("  java -jar app.jar member <port> [mode] [leaderHost] [leaderPort]");
        System.out.println("  java -jar app.jar client [host] [port] [auto|get-test] [count]");
    }
}
