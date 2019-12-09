import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The server to a Zero Knowledge proof interaction.
 * This server acts as the verifier in the IP protocol.
 */
public class ZKServer {
    static DataInputStream in;
    static DataOutputStream out;

    public static void main(String[] args) {

        if(args.length < 2) {
            System.err.println("Usage: java ZKServer <host> <port> [min_threshold]");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            ServerSocket serversocket = new ServerSocket();
            serversocket.bind(new InetSocketAddress(host, port));

            Socket socket = serversocket.accept();

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            ZKMath zkMath = new ZKMath();
            zkMath.g = Math.abs(zkMath.random.nextInt());
            zkMath.p = zkMath.genProbPrime();


            System.out.println("We will use generator " + zkMath.g + " and prime " + zkMath.p);
            out.writeLong(zkMath.g);
            out.writeLong(zkMath.p);
            out.flush();

            zkMath.y = in.readLong();

            int numIters;
            if(args.length >= 3) {
                numIters = zkMath.necessaryIterations(Double.parseDouble(args[2]));
            }
            else {
                numIters = zkMath.necessaryIterations(0.95);
            }

            System.out.println("Requiring " + numIters + " iterations.");
            out.writeInt(numIters);
            out.flush();

            boolean accept_proof = true;
            for(int i = 0; i < numIters; i++) {
                zkMath.C = in.readLong();

                zkMath.shareR = zkMath.random.nextBoolean();
                out.writeBoolean(zkMath.shareR);
                if(zkMath.shareR) {
                    System.out.println("Requesting r");
                }
                else {
                    System.out.println("Requesting (x+r) mod (p-1)");
                }
                out.flush();

                Long sharedData = in.readLong();
                if(zkMath.shareR) {
                    if(zkMath.C != zkMath.modPow(zkMath.g, sharedData, zkMath.p)) {
                        System.err.println("Proof information is incorrect.");
                        accept_proof = false;
                    }
                }
                else {
                    if((zkMath.C * zkMath.y) % zkMath.p != sharedData) {
                        System.err.println("Proof information is incorrect.");
                        accept_proof = false;
                    }
                }
                out.writeBoolean(accept_proof);
                out.flush();
                if(!accept_proof) {
                    System.err.println("Proof stopping after " + i + " iterations.");
                    break;
                }
                System.out.println();
            }

        }
        catch(IOException e){
            System.err.println("Couldn't host the server at "  + host + ":" + port);
            System.exit(1);
        }
    }
}
