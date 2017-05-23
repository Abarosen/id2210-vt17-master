package se.kth.app.sets.graph;

import se.kth.app.sets.ORSet.ORSetOperations;
import se.sics.kompics.KompicsEvent;
import se.sics.ktoolbox.util.network.KAddress;


/**
 * Created by Barosen on 2017-05-23.
 */
public class GraphOperations {

    //Add
    static class AddV implements KompicsEvent {
        Vertex v;
        AddV(Vertex v){
            this.v = v;
        }
    }

    static class AddE implements KompicsEvent {
        Edge e;
        AddE(Edge e){
            this.e = e;
        }
    }

    //Remove
    static class RemoveV implements KompicsEvent {
        Vertex v;
        RemoveV(Vertex v){
            this.v = v;
        }
    }

    static class RemoveE implements KompicsEvent {
        Edge e;
        RemoveE(Edge e){
            this.e = e;
        }
    }

    //Lookup
    static class Lookup implements KompicsEvent{
        KAddress ret;
        OpType type;
        Vertex v;
        Edge e;

        Lookup(KAddress self, Edge e){
            this.ret = self;
            this.e = e;
            type = OpType.Edge;
        }

        Lookup(KAddress self, Vertex v){
            this.ret = self;
            this.type = OpType.Vertex;
            this.v = v;
        }
    }

    //Response
    static class Response implements KompicsEvent{
        final boolean response;
        KAddress ret;
        Response(KAddress ret, boolean response){
            this.response = response;
            this.ret = ret;
        }
    }


    /**************************************************/
    /********************Internal**********************/
    /**************************************************/


    static class InternalOperation implements KompicsEvent{
        Vertex v;
        Edge e;
        OpType type;
        InternalOperation(OpType type, Vertex v){
            this.type = type;
            this.v = v;
        }
        InternalOperation(OpType type, Edge e){
            this.type = type;
            this.e = e;
        }
    }

    public static enum OpType {
        Edge, Vertex, RemoveE, RemoveV;
    }
}

