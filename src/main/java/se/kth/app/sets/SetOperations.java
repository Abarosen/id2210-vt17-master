package se.kth.app.sets;

import se.sics.kompics.KompicsEvent;

/**
 * Created by Barosen on 2017-05-18.
 */
public class SetOperations {


    //Add
    static class Add implements KompicsEvent{
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
        boolean grej;

        Response(boolean grej){
            this.grej = grej;
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
        InternalAdd(String value){
            this.value = value;
        }
    }

    static class InternalRemove implements KompicsEvent{
        String value;
        InternalRemove(String value){
            this.value = value;
        }
    }

}
