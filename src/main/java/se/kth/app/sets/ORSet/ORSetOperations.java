package se.kth.app.sets.ORSet;

import se.sics.kompics.KompicsEvent;

import java.util.Set;
import java.util.UUID;

/**
 * Created by Barosen on 2017-05-19.
 */
public class ORSetOperations {


    //Add
    public static class Add implements KompicsEvent {
        final String value;
        Add(String value){
        this.value = value;
    }
    }

    //Lookup
    public static class Lookup implements KompicsEvent{
        final String key;
        Lookup(String key){
            this.key = key;
        }
    }

    //Response
    public static class Response implements KompicsEvent{
        final boolean response;

        Response(boolean response){
            this.response = response;
        }
    }

    //Remove
    public static class Remove implements KompicsEvent{
        final String value;
        Remove(String value){
            this.value = value;
        }
    }

    /**************************************************/
    /********************Internal**********************/
    /**************************************************/

    //Add
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

