package org.polytech.pfe.domego.protocol.game;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.polytech.pfe.domego.components.business.Game;
import org.polytech.pfe.domego.components.business.Messenger;
import org.polytech.pfe.domego.components.statefull.GameInstance;
import org.polytech.pfe.domego.exceptions.MissArgumentToRequestException;
import org.polytech.pfe.domego.models.Payment;
import org.polytech.pfe.domego.models.Player;
import org.polytech.pfe.domego.models.activity.Activity;
import org.polytech.pfe.domego.models.activity.ActivityStatus;
import org.polytech.pfe.domego.protocol.EventProtocol;
import org.polytech.pfe.domego.protocol.game.key.GameRequestKey;
import org.polytech.pfe.domego.protocol.game.key.GameResponseKey;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PayResourcesEvent implements EventProtocol {

    private Map<String, ?> request;
    private GameInstance gameInstance;
    private Messenger messenger;
    private final Logger logger = Logger.getGlobal();

    public PayResourcesEvent(WebSocketSession session, Map request) {
        this.messenger = new Messenger(session);
        this.request = request;
        gameInstance = GameInstance.getInstance();

    }

    @Override
    public void processEvent() {
        try {
            this.checkArgumentOfRequest();
        }catch (MissArgumentToRequestException missArgumentToRequest){
            this.messenger.sendErrorCuzMissingArgument(missArgumentToRequest.getMissKey().getKey());
            return;
        }

        Optional<Game> optionalGame = gameInstance.getSpecificGameByID(String.valueOf(request.get(GameRequestKey.GAMEID.getKey())));
        if(optionalGame.isEmpty()){
            this.messenger.sendError("GAME NOT FOUND");
            return;
        }

        Game game = optionalGame.get();


        Optional<Player> optionalPlayer = game.getPlayers().stream().filter(player -> player.getID().equals(String.valueOf(request.get(GameRequestKey.USERID.getKey())))).findAny();

        if (optionalPlayer.isEmpty()){
            this.messenger.sendError("USER NOT FOUND");
            return;
        }

        Player player = optionalPlayer.get();

        this.payResources(game,player);

    }


    private void payResources(Game game, Player player){
        Type founderListType = new TypeToken<ArrayList<Payment>>(){}.getType();
        List<Payment> payments = new Gson().fromJson(request.get("payments").toString(), founderListType);
        Activity currentActivity = game.getCurrentActivity();
        if (!currentActivity.payResources(player, payments)){
            int totalAmount = payments.stream().mapToInt(Payment::getAmount).sum();
            logger.log(Level.INFO,"PaymentResourcesEvent : In the game {0}, the player named {1} has not enough resources, he has {2} and he need {3} to pay", new Object[]{game.getId(), player.getID(),player.getResourcesAmount(), totalAmount});
            return;
        }

        logger.log(Level.INFO,"PaymentResourcesEvent : In the game {0}, the player named {1} has realize {2} payment for the activity : {3}", new Object[]{game.getId(), player.getID(),payments.size(), currentActivity.getId()});
        new UpdatePaymentGameEvent(game, player).processEvent();
        if(currentActivity.getActivityStatus().equals(ActivityStatus.FINISHED)){
            if(currentActivity.getId() == game.getActivities().size())
                new FinishGameEvent(game).processEvent();
            else{
                game.goToTheNextActivity();
                new ChangeActivityEvent(game).processEvent();
            }
        }

        game.getPlayerById(player.getID()).ifPresent(this::sendResponseToUser);
    }

    private void sendResponseToUser(Player player) {
        JsonObject response = new JsonObject();
        response.addProperty(GameResponseKey.RESPONSE.key, "PAY_RESOURCES");
        response.addProperty(GameResponseKey.RESOURCES.key, player.getResourcesAmount());
        messenger.sendSpecificMessageToAUser(response.toString());
    }


    private void checkArgumentOfRequest() throws MissArgumentToRequestException{
        if(!request.containsKey(GameRequestKey.GAMEID.getKey()))
            throw new MissArgumentToRequestException(GameRequestKey.GAMEID);
        if(!request.containsKey(GameRequestKey.USERID.getKey()))
            throw new MissArgumentToRequestException(GameRequestKey.USERID);
        if(!request.containsKey(GameRequestKey.PAYMENTS.getKey()))
            throw new MissArgumentToRequestException(GameRequestKey.PAYMENTS);
    }
}


