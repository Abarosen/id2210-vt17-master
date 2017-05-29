package se.kth.app.sets;

import se.sics.kompics.KompicsEvent;

/**
 * Created by Barosen on 2017-05-18.
 */
public class SetOperations {


    /**************************************************/
    /********************Internal**********************/
    /**************************************************/


    //Only used internally by sets
    static class InternalOperation implements KompicsEvent{
        String value;
        OpType type;
        InternalOperation(OpType type, String value){
            this.value = value;
            this.type = type;
        }
    }
    public static enum OpType {
        Remove, Add;
    }

}
