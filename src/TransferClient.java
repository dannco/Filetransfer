import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TransferClient {
    static int port=2000;
    static String host = "localhost";

    static Scanner sc;
    static Socket con;

    public static void requestFile(String req) {
        try {
            con = new Socket(host,port);
            System.out.println("Connection to server established");
            ObjectOutputStream out = new ObjectOutputStream(con.getOutputStream());
            out.writeUTF("get\0");
            out.flush();
            out.writeUTF(req+"\0");
            out.flush();
            System.out.println("Requesting file "+req);

            ObjectInputStream in = new ObjectInputStream(con.getInputStream());

            String name = in.readUTF();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];

            int n = 0;
            while (-1 != (n = in.read(buf))) {
                output.write(buf,0,n);
            }
            output.close();
            byte[] img = output.toByteArray();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(name);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            fos.write(img);
            fos.close();
            System.out.println("File "+name+" received");
            out.close();
            con.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void transferFile(String file) {
        try {

            FileInputStream fis = new FileInputStream(file);
            System.out.println("File found");
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            System.out.println("Sending file");

            con = new Socket(host,port);
            ObjectOutputStream out = new ObjectOutputStream(con.getOutputStream());
            out.writeUTF("send\0");
            out.flush();
            out.writeUTF(file);
            out.flush();
            out.write(buffer); 
            out.close();
            con.close();
            System.out.println("File sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void requestList() {
        try {
            con = new Socket(host,port);
            ObjectOutputStream out = new ObjectOutputStream(con.getOutputStream());
            out.writeUTF("list\0");
            out.flush();
            ObjectInputStream in = new ObjectInputStream(con.getInputStream());
            String inc = in.readUTF();
            System.out.println(inc);

            in.close();
            out.close();
            con.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void pingServer() {
        try {
            System.out.println("Pinging server");

            con = new Socket(host, port);
            ObjectOutputStream out = new ObjectOutputStream(con.getOutputStream());
            long t  = System.currentTimeMillis();
            out.writeUTF("ping\0");
            out.flush();
            ObjectInputStream in = new ObjectInputStream(con.getInputStream());
            if (in.readUTF().trim().equals("OK")) {
                System.out.println("Server response. Latency: "+(System.currentTimeMillis()-t)+"ms");
            }
        } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Connection failed");
        }
    }


    public static void main(String[] args) {
        sc = new Scanner(System.in);
        if (args.length>0) {
            host=args[0];
        } if (args.length>1) {
            port = Integer.parseInt(args[1]);
        }
        String in;
        boolean help=true;
        while (true) {
            if (help) {
                System.out.println("Connection settings: "+host+" : "+port+"\n" +
                        "Commands:\n\"ping\" - check connection to server\n" +
                        "\"send <file>\" - send image at given path to server\n" +
                        "\"list\" - retrieve list of images stored by server\n" +
                        "\"get <file>\" - retrieve file from server\n" +
                        "\"quit\" - disconnect from server\n" +
                        "\"help\" - display these commands again\n" +
                        "\"set\" - adjust server connection settings");
                help=false;
            }
            System.out.print("Command: ");
            in = sc.nextLine();
            if (in.matches("get .*")) requestFile(in.split(" ", 2)[1]);
            else if (in.matches("ping")) pingServer();
            else if (in.matches("send .*")) transferFile(in.split(" ",2)[1]);
            else if (in.matches("list")) requestList();
            else if (in.matches("help")) help=true;
            else if (in.matches("quit")) break;
            else if (in.matches("set")) setServer();
            else System.out.println("Unknown command");
        }
    }

    public static void setServer() {
        System.out.println("Enter new host and port. Leave field empty to skip.");
        System.out.print("Host (currently "+host+"): ");
        try {
        String in = sc.nextLine();
        host = in.equals("")? host : in;
        System.out.print("Port (currently "+port+"): ");
        in = sc.nextLine();
        port = in.equals("")? port : Integer.parseInt(in);
        } catch (Exception e) {
            System.out.println("Invalid input");
        }
    }
}
