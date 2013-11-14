import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RingElection {
  
  int     coord;       /* Initialize it to the ID of the Co-Ordinator */
  int     curPid;      /* Initialize it to the ID of the current node */
  int     n;           /* Initialize it to the number of nodes */

  /* flag variables */

  int     elNum    = 1;
  int     c_alive  = -1;
  
  String[]  address;   /* Initialize the array to the IP addresses of the next occuring nodes in order */

  String[]  sAddress;  /* Initialize the array to the IP addresses of all the nodes from node 1 */

  String    cdAddress; /* Initialize it to the IP address of the Co-Ordinator */
  String    address1;  /* Initiliaze it to the IP address of the current node */

  int       cdPort;    /* Initialize it to the port number of the Co-Ordinator */

  int[]     port;      /* Initialize the array to the port number of the next occuring nodes in order */

  int       port1;     /* Initialize it to a port number */
  int       port2;     /* Initialize it to a port number */
  int       port3;     /* Initialize it to a port number */
  int       port4;     /* Initialize it to a port number */
  int       port5;     /* Initialize it to a port number */
  int       port6;     /* Initialize it to a port number */

  int[]     sPorts;     /* Initialize it to port numbers of the other nodes */

  int[]     cPorts;     /* Initialize it to port numbers of the other nodes */ 

  int[]     mPorts;     /* Initialize it to port numbers of the other nodes */

  DatagramSocket socket1    = new DatagramSocket(port1);
  DatagramSocket socket2    = new DatagramSocket(port2);
  DatagramSocket socket3    = new DatagramSocket(port3);
  DatagramSocket socket4    = new DatagramSocket(port4);
  DatagramSocket socket5    = new DatagramSocket(port5);
  DatagramSocket socket6    = new DatagramSocket(port6);
  
  Queue<String>  queue      = new LinkedList<String>();

  String  cdMsg             = "COORDNTR ";
  String  okMsg             = "OKMESAGE ";
  String  elMsg             = "ELECTION ";
  
  byte[]  cdAck             = new byte[1024];
  byte[]  okAck             = new byte[1024];
  byte[]  elAck             = new byte[1024];

  Runnable       one,     two,     three,   four;
  Thread         thread1, thread2, thread3, thread4;
  Communication  conObj;

  String         uname      = "Node: " + curPid;

  RingElection() throws Exception {

    one     = new RecvThread();
    two     = new SendThread();
    three   = new CRThread();
    four    = new CSThread();

    thread1 = new Thread(one);
    thread2 = new Thread(two);
    thread3 = new Thread(three);
    thread4 = new Thread(four);

    thread2.start();
    thread1.start();
    thread4.start();
    thread3.start();

    conObj  = new Communication();

    StartProcess();

    Thread.currentThread().sleep(3000);

  }

  void StartProcess()  {

    DatagramPacket cdSend, cdRecv, cdSend2;

    try {  

      byte[]  cdsMsg  =  "ALIVE".getBytes();
      byte[]  cdrMsg  =  new byte[1024];

      System.out.println("Node " + curPid + " Started");

      while(true) {

        cdRecv    = new DatagramPacket(cdrMsg,   cdrMsg.length);
      
        if(curPid != coord) {

          while(c_alive != 0) {

            cdSend    = new DatagramPacket(cdsMsg,  cdsMsg.length, InetAddress.getByName(cdAddress), cdPort);
            socket1.send(cdSend);

            try {
              socket1.setSoTimeout(3000);
              socket1.receive(cdRecv);
            }
            catch(Exception e) {
              c_alive = 0; elNum = 0;
            }  
          }
    
        }

        else if(curPid == coord) {

          System.out.println("I'm the co-ordinator");

          while(curPid == coord) {
  
            socket1.receive(cdRecv);
            cdSend2    = new DatagramPacket(cdsMsg,  cdsMsg.length, cdRecv.getAddress(), cdRecv.getPort());
            socket1.send(cdSend2);
          }
        }
      }
    }
    catch(Exception e) {
    }  
  }

  class CRThread implements Runnable {
  
    public void run() {
      
      System.out.println("Receive thread started");

      try {
        while(true) {

          byte[] msg  = new byte[1024];
          byte[] kMsg = okMsg.getBytes();
        
          int    temp;
    
          DatagramPacket recvPacket = new DatagramPacket(msg, msg.length);
    
          socket4.receive(recvPacket);

          String str  = new String(recvPacket.getData());
  
          temp        = Character.getNumericValue(str.charAt(0));

          if(temp > coord) {

            coord     = temp;
            cdAddress = sAddress[coord-1];
            cdPort    = sPorts[coord-1];

            System.out.println("Node " + coord + " restarted");

            while(!queue.isEmpty())
              queue.remove();

            c_alive   = 0;
            elNum     = 0;
          }
        }
      }
      catch(Exception e) {
        System.out.println("ERROR in Server Check Receive Thread");
      }
    }
  }

  class CSThread implements Runnable {
    
    public void run() {

      System.out.println("Send Thread started");  

      try {

        String str    = Integer.toString(curPid);
        byte[] elByte = str.getBytes();

        while(true) {
         
          for(int i = 0; i < n; i++) {
            if(i != curPid) {
              DatagramPacket sendPacket = new DatagramPacket(elByte, elByte.length, InetAddress.getByName(sAddress[i]), cPorts[i]);
              Thread.currentThread().sleep(250);
              socket5.send(sendPacket);
            }
          }  
        }
      }
      catch(Exception e) {
        System.out.println("ERROR in Server Check Send Thread");
      }
    }
  }

  class RecvThread implements Runnable {

    public void run() {

      try {

        while(true) {

          byte[] msg  = new byte[1024];
          byte[] kMsg = okMsg.getBytes();

            Thread.currentThread().sleep(6000); 
    
            DatagramPacket recvPacket = new DatagramPacket(msg, msg.length);

            try {  
  
              socket2.receive(recvPacket);

              DatagramPacket sendPacket = new DatagramPacket(kMsg, kMsg.length, recvPacket.getAddress(), recvPacket.getPort());

              String str    = new String(recvPacket.getData());
              str           = str.trim();
              String subStr = str.substring(0, 8);

              int len       = str.length();

              System.out.println("Received    : " + str);
  
              if(subStr.equals("ELECTION")) {
  
                socket2.send(sendPacket);

                if(Character.getNumericValue(str.charAt(9)) != curPid) {

                  str = str + Integer.toString(curPid);
                  queue.add(str);
                }
                else {
  
                  int[] nodes = new int[10];
                  int t = 0, grtNode = 0;

                  for(int i = 9; i < len; i++)
                    nodes[t++] = Character.getNumericValue(str.charAt(i));

                  for(int i = 0; i < t; i++) {
                    if(grtNode < nodes[i])
                      grtNode = nodes[i];
                  }
  
                  String temp = cdMsg;
                  temp        = temp + Integer.toString(grtNode);

                  coord       = grtNode;
                  cdPort      = sPorts[grtNode-1];
                  cdAddress   = address[grtNode-1];
                  System.out.println("Co-Ordinator: Node " + grtNode);
                  c_alive = 1; elNum = 1;

                  queue.add(temp);
                }
              }
  
              if(subStr.equals("COORDNTR")) {

                socket2.send(sendPacket);

                if(Character.getNumericValue(str.charAt(9)) != curPid) {
                  coord     = Character.getNumericValue(str.charAt(9));
                  cdPort    = sPorts[coord-1];
                  cdAddress = address[coord-1];
                  System.out.println("Co-Ordinator: Node " + coord);
                  c_alive = 1; elNum = 1;
                  queue.add(str);
                }
                else {
                  coord     = curPid;
                  cdPort    = port1;
                  cdAddress = address1;
                  c_alive   = 1; elNum = 1;
                  System.out.println("I'm the co-ordinator");
                }
              }
            }
            catch(Exception e) {
            }
        }
      }
      catch(Exception e) {
        System.out.println("ERROR in thread 1" + e);
      }
     
    }
  }

  class SendThread implements Runnable {

    public void run() {

      try {

        while(true) {

          int live_node  = 0;
          byte[] msg    = new byte[1024];

          Thread.currentThread().sleep(3000);
          System.out.print("");

          if(elNum == 0) {

            elNum = 1;

            String str    = elMsg + Integer.toString(curPid);

            byte[] elByte = str.getBytes();

            for(int i = 0; i < n-1; i++) {
              DatagramPacket recvPacket = new DatagramPacket(msg, msg.length);
              DatagramPacket sendPacket = new DatagramPacket(elByte, elByte.length, InetAddress.getByName(address[i]), port[i]);

              socket3.send(sendPacket);

              try {
                live_node = (curPid % n) + i + 1;
                socket3.setSoTimeout(3000);
                socket3.receive(recvPacket);
                String okAck = new String(recvPacket.getData());
                System.out.println("Sent        : " + str);
                break;
              }
              catch(Exception e) {
              }
            }
          }

          while((!queue.isEmpty())) {
    
            String msStr  = new String(queue.remove());
            byte[] msByte = msStr.getBytes();

            for(int i = 0; i < n-1; i++) {

              DatagramPacket recvPacket = new DatagramPacket(msg, msg.length);
              DatagramPacket sendPacket = new DatagramPacket(msByte, msByte.length, InetAddress.getByName(address[i]), port[i]);

              socket3.send(sendPacket);
  
              try {
                live_node = (curPid % n) + i + 1;
                socket3.setSoTimeout(1000);
                socket3.receive(recvPacket);
                String okAck = new String(recvPacket.getData());
                System.out.println("Sent        : " + msStr);
                break;
              }
              catch(Exception e) {      
              }
            }
          }
        }
      }
      catch(Exception e) {
        System.out.println("ERROR" + e);
      }     
    }
  }

  class Communication extends JFrame implements ActionListener {

    JTextArea  taMessages;
    JTextField tfInput;
    JButton btnSend,btnExit;
    
    public Communication() throws Exception {
    
      super(uname);
      buildInterface();
      new MessagesThread().start();  
    }
    
    public void buildInterface() {
      btnSend = new JButton("Send");
      btnExit = new JButton("Exit");
      taMessages = new JTextArea();
      taMessages.setRows(10);
      taMessages.setColumns(50);
      taMessages.setEditable(false);
      tfInput  = new JTextField(50);
      JScrollPane sp = new JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      add(sp,"Center");
      JPanel bp = new JPanel( new FlowLayout());
      bp.add(tfInput);
      bp.add(btnSend);
      bp.add(btnExit);
      add(bp,"South");
      btnSend.addActionListener(this);
      btnExit.addActionListener(this);
      setSize(500,300);
      setVisible(true);
      pack();
    }
      
    public void actionPerformed(ActionEvent evt) {

      if (evt.getSource() == btnExit ) {
        System.exit(0);
      } 

      else {
  
        String str;

        if((curPid == coord) && (queue.isEmpty())) {

          for(int i = 0; i < n; i++) {

            str        = tfInput.getText(); //send  
            byte[] msg = str.getBytes();

            if(i != curPid-1) {
              try {
                DatagramPacket sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getByName(sAddress[i]), mPorts[i]);
                socket6.send(sendPacket);
              }
              catch(Exception e) {
              }
            }
            str = "";
          }
        }    
      }
    }
    
    
    class  MessagesThread extends Thread {
  
      public void run() {

        String prev  = "";
  
        try {

          while(true) {

            if(curPid != coord) {

              byte[] msg   = new byte[1024];

              DatagramPacket recvPacket = new DatagramPacket(msg, msg.length);

              socket6.receive(recvPacket);

              String str  = new String(recvPacket.getData());

              str         = str.trim();

              if(str.equals(prev)) 
                str = "Receiving same message";
              else
                prev = str;

              str = "<Co-ordinator>: " + str;

              taMessages.append(str + "\n");//receive
              str = "";
            }
          } 
        } 
        catch(Exception ex) {
        }
      }
    }
  }

  public static void main(String args[]) throws Exception {

    RingElection obj = new RingElection();

  }
}


