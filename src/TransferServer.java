import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TransferServer{
    ServerSocket sock;
    ByteArrayOutputStream output;

    static int port = 2000;

    public TransferServer() {
        Scanner sc = new Scanner(System.in);
        String in;
        while (true) {
            System.out.print("Server set to port "+port+". \nEnter new port or leave field empty to confirm. ");
            in = sc.nextLine().trim();
            if (in.equals("")) break;
            try {
                port = Integer.parseInt(in);
            } catch (Exception e) {
                System.out.println("Invalid input.");
            }
        }
        try {
            sock = new ServerSocket(port);
            System.out.println("Server started");

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void run() {
        try {
            while (true) {
                Socket con = sock.accept();
                new Request(con);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void main(String[] args) {
        new TransferServer().run();
    }

    class Request extends Thread {
        Socket s;
        ObjectInputStream in;
        public Request(Socket con) {
            s = con;
            System.out.println("Receiving request from client...");
            run();
        }

        public void run() {
            try {
                in = new ObjectInputStream(s.getInputStream());
                String req = in.readUTF().trim();

                if (req.matches("get")) sendFile();
                else if (req.matches("send")) getFile();
                else if (req.matches("list")) listFiles();
                else if (req.matches("ping")) {
                    System.out.println("Pinged by client");
                    ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                    out.writeUTF("OK\0");
                    out.flush();
                    out.close();
                }
                else {
                    System.out.println("No match");
                }

                System.out.println("Request fulfilled");

                in.close();
                s.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendFile() {
            try {

                String file = in.readUTF().trim();
                System.out.println("Client requesting file: "+file);
                FileInputStream fis = new FileInputStream(file);
                System.out.println("File found");
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                System.out.println("Sending file");

                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                out.writeUTF(file);
                out.flush();
                out.write(buffer); 
                out.close();
                System.out.println("File sent");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void getFile() {
            try {
                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());

                String name = in.readUTF();
                System.out.println("Client sending file "+name);

                output = new ByteArrayOutputStream();
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


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void listFiles() {
            try {
                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                System.out.println("Client requesting list of stored files");

                File dir = new File(".");
                File[] filesList = dir.listFiles();
                String s = "Contents of server directory:\n";
                for (File file : filesList) {
                    if (file.isFile()) {
                        s+=file.getName()+"\n";
                    }
                }
                out.writeUTF(s);
                out.flush();
                out.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
