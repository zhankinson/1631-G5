/*
A Simple Example--Authentication Component.
To Create a Component which works with the InterfaceServer,
the interface ComponentBase is required to be implemented.

interface ComponentBase is described in InterfaceServer.java.

*/

import java.io.*;
import java.util.*;

public class MyComponent implements ComponentBase{

    private final int init=0;
    private final int success=1;
    private final int failure=2;


    private int state;

    public MyComponent(){
        state=init;
    }

/* just a trivial example */

    private void doAuthentication(String first,String last,String passwd){

        if (first.equals("xin")&&last.equals("li")&&passwd.equals("xl123"))
            state=success;
        else
            state=failure;
    }

/* function in interface ComponentBase */

    public KeyValueList processMsg(KeyValueList kvList){
        int MsgID=Integer.parseInt(kvList.getValue("MsgID"));
        if (MsgID!=0) return null;
        doAuthentication(kvList.getValue("FirstName"),kvList.getValue("LastName"),kvList.getValue("passwd"));
        KeyValueList kvResult = new KeyValueList();
        kvResult.addPair("MsgID","1");
        kvResult.addPair("Description","Authentication Result");

        switch (state) {
            case success: {
                kvResult.addPair("Authentication","success");
                break;
            }
            case failure: {
                kvResult.addPair("Authentication","failure");
                break;
            }
        }
        return kvResult;
    }

}
