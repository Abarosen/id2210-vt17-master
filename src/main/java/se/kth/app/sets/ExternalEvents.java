package se.kth.app.sets;

import se.sics.kompics.KompicsEvent;
import se.sics.ktoolbox.util.network.KAddress;

/**
 * Created by Barosen on 2017-05-24.
 */

public class ExternalEvents {
    //Add
    public static class Add implements KompicsEvent {
        public final String value;
        public Add(String value){
            this.value = value;
        }
    }

    //Lookup
    public static class Lookup implements KompicsEvent{
        public final String key;
        public final KAddress ret;
        public Lookup(String key){
            this.key = key;
            this.ret = null;
        }
        public Lookup(KAddress ret, String key){
            this.key = key;
            this.ret = ret;
        }
    }

    //Response
    public static class Response implements KompicsEvent{
        public final boolean res;
        public final String key;
        public final KAddress ret;


        public Response(KAddress ret, String key, boolean res){
            this.res = res;
            this.key = key;
            this.ret = ret;
        }
    }

    //Remove
    public static class Remove implements KompicsEvent{
        public final String value;
        public Remove(String value){
            this.value = value;
        }
    }
}
