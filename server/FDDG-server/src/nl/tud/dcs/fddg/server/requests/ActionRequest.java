package nl.tud.dcs.fddg.server.requests;

import nl.tud.dcs.fddg.game.actions.Action;

/**
 * Class that represents the request for an Action that is sent from one server to the others.
 */
public class ActionRequest {

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
}
