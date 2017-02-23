/*

Communication Interface for Virtual Classroom Components.
Complied in JDK 1.4.2
Usage:

Step1: Create your component which implements Interface
ComponentBase.

Step2: Replace the ComponentMy with your class name in
InterfaceServer::main().

Step3: start up Interface Server by
java InterfaceServer

Step4: Use Virtual remote which is also provided on the
web to send message and get the feedback message.

*/


import java.io.*;
import java.net.*;
import java.util.*;

/*
Class KeyValueList:
List of (Key, Value) pair--the basic format of message
Keys: MsgID and Description are required for any messages
MsgID 0-999 is reserved for system use.
You MsgID could start from 1000.
*/

class KeyValueList{
    private Vector Keys;
    private Vector Values;

    /* Constructor */
    public KeyValueList(){
        Keys=new Vector();
        Values=new Vector();
    }

/* Look up the value given key, used in getValue() */

    private int lookupKey(String strKey){
        for(int i=0;i<Keys.size();i++){
            String k=(String) Keys.elementAt(i);
            if (strKey.equals(k)) return i;
        }
        return -1;
    }

/* add new (key,value) pair to list */

    public boolean addPair(String strKey,String strValue){
        return (Keys.add(strKey)&& Values.add(strValue));
    }

/* get the value given key */

    public String getValue(String strKey){
        int index=lookupKey(strKey);
        if (index==-1) return null;
        return (String) Values.elementAt(index);
    }

    /* Show whole list */
    public String toString(){
        String result = new String();
        for(int i=0;i<Keys.size();i++){
            result+=(String) Keys.elementAt(i)+":"+(String) Values.elementAt(i)+"\n";
        }
        return result;
    }

    public int size(){ return Keys.size(); }

    /* get Key or Value by index */
    public String keyAt(int index){ return (String) Keys.elementAt(index);}
    public String valueAt(int index){ return (String) Values.elementAt(index);}
}

/*
Class MsgEncoder:
Serialize the KeyValue List and Send it out to a Stream.
*/
class MsgEncoder{
    private PrintStream printOut;
    /* Default of delimiter in system is $$$ */
    private final String delimiter="$$$";

    public MsgEncoder(){
    }

/* Encode the Key Value List into a string and Send it out */

    public void sendMsg(KeyValueList kvList, OutputStream out) throws IOException{
        PrintStream printOut= new PrintStream(out);
        if (kvList==null) return;
        String outMsg= new String();
        for(int i=0; i<kvList.size();i++){
            if (outMsg.equals(""))
                outMsg=kvList.keyAt(i)+delimiter + kvList.valueAt(i);
            else
                outMsg+=delimiter+kvList.keyAt(i)+delimiter + kvList.valueAt(i);
        }
//System.out.println(outMsg);
        printOut.println(outMsg);
    }
}

/*
Class MsgDecoder:
Get String from input Stream and reconstruct it to
a Key Value List.
*/

class MsgDecoder {

    private BufferedReader bufferIn;
    private final String delimiter="$$$";

    public MsgDecoder(InputStream in){
        bufferIn = new BufferedReader(new InputStreamReader(in));
    }

/*
get String and output KeyValueList
*/

    public KeyValueList getMsg() throws IOException{
        String strMsg= bufferIn.readLine();

        if (strMsg==null) return null;

        KeyValueList kvList = new KeyValueList();
        StringTokenizer st = new StringTokenizer(strMsg,delimiter);
        while (st.hasMoreTokens()) {
            kvList.addPair(st.nextToken(),st.nextToken());
        }
        return kvList;
    }

}

/*
interface ComponentBase:
The interface you have to implement in your component
*/
interface ComponentBase{
    public KeyValueList processMsg(KeyValueList kvList);
}

/*
Class InterfaceServer
Set up a socket server waiting for the remote to connect.
*/

public class InterfaceServer
{

    public static final int port=7999;

    public static void main(String[] args) throws Exception
    {
        ServerSocket server = new ServerSocket(port);

    /*
    You need to create your component here
    */

        ComponentBase compMy= new MyComponent();
        Socket client = server.accept();
        try{
            MsgDecoder mDecoder= new MsgDecoder(client.getInputStream());
            MsgEncoder mEncoder= new MsgEncoder();
            KeyValueList kvInput,kvOutput;
            do
            {
                kvInput=mDecoder.getMsg();
                if (kvInput!=null) {
                    System.out.println("Incomming Message:\n");
                    System.out.println(kvInput);
                    KeyValueList kvResult=compMy.processMsg(kvInput);
                    System.out.println("Outgoing Message:\n");
                    System.out.println(kvResult);
                    mEncoder.sendMsg(kvResult,client.getOutputStream());
                }
            }
            while (kvInput!=null);
        }
        catch (SocketException e){
            System.out.println("Connection was Closed by Client");
        }
    }
}
