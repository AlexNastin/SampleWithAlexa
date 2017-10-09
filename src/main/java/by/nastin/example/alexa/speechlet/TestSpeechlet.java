package by.nastin.example.alexa.speechlet;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This sample shows how to create a simple speechlet for handling intent requests and managing
 * session interactions.
 */
public class TestSpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(TestSpeechlet.class);

    private static final String BANDWIDTH_KEY = "BANDWIDTH";
    private static final String DURATION_KEY = "DURATION";
    private static final String BANDWIDTH_SLOT = "Bandwidth";
    private static final String DURATION_SLOT = "Duration";
    private long credits = 0;

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session) throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        // Get intent from the request object.
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        // Note: If the session is started with an intent, no welcome message will be rendered;
        // rather, the intent specific response will be returned.
        if ("UpBandwidthIntent".equals(intentName)) {
            return upBandwidth(intent, session);
        } else if ("TellBandwidthIntent".equals(intentName)) {
            return tellBandwidth(intent, session);
        } else if ("TellCreditsIntent".equals(intentName)) {
            return tellCredits(intent, session);
        } else if ("AddCreditsIntent".equals(intentName)) {
            return addCredits(intent, session);
        } else if ("NoAddCreditsIntent".equals(intentName)) {
            return noAddCredits(intent, session);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session) throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     * @return SpeechletResponse spoken and visual welcome message
     */
    private SpeechletResponse getWelcomeResponse() {
        // Create the welcome message.
        String speechText = "Welcome to the Alexa Skills Kit sample.";
        String repromptText = "Please tell me your favorite color by saying, my favorite color is red";
        return getSpeechletResponse(speechText, repromptText, true);
    }


    private SpeechletResponse upBandwidth(final Intent intent, final Session session) {
        Slot bandwidthSlot = intent.getSlot(BANDWIDTH_SLOT);
        Slot durationSlot = intent.getSlot(DURATION_SLOT);
        String speechText, repromptText;
        if (bandwidthSlot != null && durationSlot != null) {
            String bookedBandwidth = bandwidthSlot.getValue();
            String bookedDuration = durationSlot.getValue();
            session.setAttribute(BANDWIDTH_KEY, bookedBandwidth);
            session.setAttribute(DURATION_KEY, bookedDuration);
            speechText = String.format("Your booked bandwidth is %s and booked duration %s", bookedBandwidth, bookedDuration);
            repromptText = String.format("Your booked bandwidth is %s and booked duration %s", bookedBandwidth, bookedDuration);
        } else {
            speechText = "I do not understand you, please try again";
            repromptText = "I do not understand you, please try again";
        }
        return getSpeechletResponse(speechText, repromptText, true);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and get the user's favorite color from the
     * Session.
     * @param intent intent for the request
     * @return SpeechletResponse spoken and visual response for the intent
     */
    private SpeechletResponse tellBandwidth(final Intent intent, final Session session) {
        String speechText;
        boolean isAskResponse = false;
        String bookedBandwidth = (String) session.getAttribute(BANDWIDTH_KEY);
        String bookedDuration = (String) session.getAttribute(DURATION_KEY);
        if (StringUtils.isNotEmpty(bookedBandwidth) && StringUtils.isNotEmpty(bookedDuration)) {
            speechText = String.format("Your bandwidth is %s bandwidth for the next %s. Goodbye.", bookedBandwidth, bookedDuration);
        } else {
            speechText = "I'm not sure what about your bandwidth is.";
            isAskResponse = true;
        }
        return getSpeechletResponse(speechText, speechText, isAskResponse);
    }

    private SpeechletResponse tellCredits(Intent intent, Session session) {
        String speechText = null;
        if (credits == 0) {
            speechText = "You dont have any movie credits available. Would you like to purchase 5 credits for $12.99?";
            credits = 5;
        } else {
            speechText = String.format("You have is %s movie credits available", credits);
        }
        return getSpeechletResponse(speechText, speechText, true);
    }

    private SpeechletResponse addCredits(Intent intent, Session session) {
        String speechText = "Ok. You now have 5 credits";
        return getSpeechletResponse(speechText, speechText, true);
    }

    private SpeechletResponse noAddCredits(Intent intent, Session session) {
        String speechText = "As you wish";
        return getSpeechletResponse(speechText, speechText, true);
    }

    /**
     * Returns a Speechlet response for a speech and reprompt text.
     */
    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText, boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("TestSpeechlet");
        card.setContent(speechText);
        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);
        if (isAskResponse) {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);
            return SpeechletResponse.newAskResponse(speech, reprompt, card);
        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }
}
