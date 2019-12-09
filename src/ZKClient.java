import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * The client to a Zero Knowledge proof interaction.
 * This client acts as the prover in the IP protocol.
 */
public class ZKClient {
    public static void main(String[] args) {
        if(args.length < 2) {
            System.err.println("Usage java ZKClient <host> <port>");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port));
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            ZKMath zkMath = new ZKMath();
            zkMath.x = Math.abs(zkMath.random.nextInt());
            System.out.println("Private key: " + zkMath.x);

            // Read in generator and prime from verifier
            zkMath.g = in.readLong();
            zkMath.p = in.readLong();

            System.out.println("y = " + zkMath.y);
            zkMath.y = zkMath.modPow(zkMath.g, zkMath.x, zkMath.p);
            out.writeLong(zkMath.y);
            out.flush();

            int numIters = in.readInt();
            for(int i = 0; i < numIters; i++) {
                zkMath.r = Math.abs(zkMath.random.nextInt());
                zkMath.C = zkMath.modPow(zkMath.g, zkMath.r, zkMath.p);
                System.out.println("r = " + zkMath.r);
                System.out.println("C = " + zkMath.C);
                out.writeLong(zkMath.C);
                out.flush();

                zkMath.shareR = in.readBoolean();
                if(zkMath.shareR) {
                    out.writeLong(zkMath.r);
                }
                else {
                    out.writeLong((zkMath.x + zkMath.r) % (zkMath.p - 1));
                }
                out.flush();
                boolean continueProof = in.readBoolean();
                if(!continueProof) {
                    System.err.println("Proof stopping after " + i + " iterations.");
                    break;
                }
                System.out.println();
            }

        } catch (IOException e) {
            System.err.println("Couldn't connect to server at "  + host + ":" + port);
            System.exit(1);
        }
    }
}