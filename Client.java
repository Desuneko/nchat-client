package heaven.nchat.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;

import javax.swing.*;


public class Client extends JFrame {

	public static ArrayList<String> msgs = new ArrayList<String>();
	/**
	 * 
	 */
	private static final long serialVersionUID = 5625186296847650892L;

	JPanel mainPanel = new JPanel();
	JTextField msgField = new JTextField();
	JButton submit = new JButton("Submit");
	public static JList<Object> msgList;
	Socket connectSocket;
	OutputStream outs;
	BufferedOutputStream out;
	DataOutputStream put;
	InputStream ins;
	DataInputStream in;
	String username = "user";
	String currentHost = "";
	boolean connected = false;
	public Client() {
		 setTitle("nchat-client");
	     setSize(600, 400);
	     setResizable(false);
	     setLocationRelativeTo(null);
	     setDefaultCloseOperation(EXIT_ON_CLOSE);
	     getContentPane().add(mainPanel);

	     
	     msgList = new JList<Object>(msgs.toArray());
	     JScrollPane scrollPane = new JScrollPane(msgList);
	     scrollPane.setMinimumSize(new Dimension(0, 400));
	     mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
	     mainPanel.add(scrollPane, BorderLayout.CENTER);
	     JPanel textPanel = new JPanel();
	     textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.LINE_AXIS));
	     textPanel.setMaximumSize(new Dimension(600, 30));
	     msgField.setPreferredSize(new Dimension(0, 30));
	     submit.setPreferredSize(new Dimension(100, 30));
	     textPanel.add(msgField);
	     textPanel.add(submit);
	     mainPanel.add(textPanel, BorderLayout.SOUTH);
	     msgField.addKeyListener(new KeyAdapter() {
	            @Override
	            public void keyPressed(KeyEvent e) {
	            	if (e.getKeyCode() == KeyEvent.VK_ENTER)
	            	{
	            		postMessage();
	            	}
	            }
	     });
         submit.addActionListener(action);
	    
	     
	}
	private Action action = new AbstractAction() {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == submit)
			{
				postMessage();
			}
        }
    };
	
	public void addToMsgList(String text)
	{
		msgs.add(text);
	    msgList.setListData((Object[])msgs.toArray());
	}
	public void connect(String host, String nickname)
	{
	    addToMsgList("Connecting to "+host);
		try {
			connectSocket = new Socket(host, 14668);
		
			outs = connectSocket.getOutputStream();
    		out = new BufferedOutputStream(outs);
    		put = new DataOutputStream(out);
    		ins = connectSocket.getInputStream();
    		in = new DataInputStream(ins);
    		put.writeByte(0x02);
    		put.writeByte(1);
			put.writeShort(nickname.length());
			put.writeChars(nickname);
			put.flush();
			currentHost = host;
			connected = true;
    		new Thread () {
    			boolean noErrors = true;
    			 public void run() {
    				 int packetId = 0;
    				    while(noErrors)
    				    {
    				    	
    				    	try {
								if (ins.available() > 0)
								{
									packetId = ins.read();
								    switch(packetId)
									{
										
										case 0x04:
											short msgLenght = in.readShort();
											String msg2 = "";
											for (int i = msgLenght; i > 0; i--)
											{
												msg2 += in.readChar();
											}
											final String msg = msg2;
											SwingUtilities.invokeLater(new Runnable() {
									            public void run() {
									                addToMsgList(msg);
									            }
									        });
											break;
										
									}
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
    				    }
    	            }
    		}.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			addToMsgList("Cannot connect to "+host);
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Client ex = new Client();
                ex.setVisible(true);
            }
        });

	}


	public void postMessage()
	{
		String msg = msgField.getText();
		if (msg != null)
		{
			if (msg.startsWith("/connect"))
			{
				try {
					put.close();
					in.close();
				    ins.close();
				    out.close();
				    outs.close();
				    connectSocket.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
				if (connected)
				{
				    addToMsgList("Disconnected from "+currentHost);
				    connected=false;
				    currentHost="";
					
				}
				connect(msg.replace("/connect ",  ""), username);
			} else if (msg.startsWith("/nick")) 
			{
				 username = msg.replace("/nick ",  "");
				 addToMsgList("Nickname changed to: "+username);
			} else {
				try {
					if (connected)
					{
						put.write((byte) 0x03);
						put.writeShort(msg.length());
						put.writeChars(msg);
						put.flush();
					} else {
						addToMsgList("Not connected!");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			msgField.setText("");
		}
	}
	

	

}
