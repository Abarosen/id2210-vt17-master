package se.kth.app.sets.ORSet;

import se.sics.kompics.KompicsEvent;

import java.util.Set;
import java.util.UUID;

/**
 * Created by Barosen on 2017-05-19.
 */
public class ORSetOperations {




    /**************************************************/
    /********************Internal**********************/
    /**************************************************/

    //Only used internally by ORSet components
    static class InternalOperation implements KompicsEvent{
        String value;
        UUID id;
        Set<UUID> ids;
        OpType type;

        InternalOperation(String value, UUID id){
            //Add
            this.type = OpType.Add;
            this.value = value;
            this.id = id;
        }

        InternalOperation(String value, Set<UUID> ids){
            //Remove
            this.type = OpType.Remove;
            this.value = value;
            this.ids = ids;
        }
    }
    public static enum OpType {
        Remove, Add;
    }
}

