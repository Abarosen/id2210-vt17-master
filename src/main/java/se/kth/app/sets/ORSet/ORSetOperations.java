package se.kth.app.sets.ORSet;

import se.sics.kompics.KompicsEvent;

import java.util.Set;
import java.util.UUID;

/**
 * Created by Barosen on 2017-05-18.
 */
public class ORSetOperations {


//Add
static class Add implements KompicsEvent {
    String value;
    Add(String value){
        this.value = value;
    }
}

    //Lookup
    static class Lookup implements KompicsEvent{
        String key;
        Lookup(String key){
            this.key = key;
        }
    }

    //Response
    static class Response implements KompicsEvent{
        final boolean response;

        Response(boolean response){
            this.response = response;
        }
    }

    //Remove
    static class Remove implements KompicsEvent{
        String value;
        Remove(String value){
            this.value = value;
        }
    }

    /**************************************************/
    /********************Internal**********************/
    /**************************************************/

    //Add
    static class InternalAdd implements KompicsEvent{
        String value;
        UUID id;
        InternalAdd(String value, UUID id){
            this.value = value;
            this.id = id;
        }
    }

    static class InternalRemove implements KompicsEvent{
        String value;
        Set<UUID> id;
        InternalRemove(String value, Set<UUID> id){
            this.value = value;
            this.id = id;
        }
    }

}

