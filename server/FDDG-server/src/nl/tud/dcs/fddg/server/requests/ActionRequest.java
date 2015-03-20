package nl.tud.dcs.fddg.server.requests;

import nl.tud.dcs.fddg.game.actions.Action;

import java.io.Serializable;

/**
 * Class that represents the request for an Action that is sent from one server to the others.
 */
public class ActionRequest implements Serializable{

    private int requestID;
    private Action theAction;

    /**
     * Constructor: initializes the instance variables
     *
     * @param requestID The ID of this request (used to send acknowledgements)
     * @param theAction The Action that the server wants to perform
     */
    public ActionRequest(int requestID, Action theAction) {
        this.requestID = requestID;
        this.theAction = theAction;
    }

    /**
     * @return The id of the request
     */
    public int getRequestID() {
        return requestID;
    }

    /**
     * @return The Action that is requested
     */
    public Action getAction() {
        return theAction;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ActionRequest){
            ActionRequest that = (ActionRequest) obj;
            return this.requestID == that.getRequestID();
        }
        return false;
    }
}
