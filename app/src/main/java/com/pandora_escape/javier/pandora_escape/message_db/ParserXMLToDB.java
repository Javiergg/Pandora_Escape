package com.pandora_escape.javier.pandora_escape.message_db;

import android.content.res.XmlResourceParser;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 *
 * Created by javier on 19/07/15.
 */
public class ParserXMLToDB {

    // No namespaces used
    private static final String ns = null;
    // Set element name
    public static final String TAG_MESSAGE_SETS = "MessageSets";
    public static final String TAG_SET          = "set";
    // Set attributes and elements
    public static final String ATT_LANGUAGE  = "language";
    public static final String TAG_MESSAGE   = "message";
    // Message elements
    public static final String TAG_CODE  = MessagesContract.COLUMN_NAME_CODE;
    public static final String TAG_TITLE = MessagesContract.COLUMN_NAME_TITLE;
    public static final String TAG_BODY  = MessagesContract.COLUMN_NAME_BODY;


    // Member variables
    private MessagesDBHelper mDBHelper;


    /**
     * Parser that takes an XML file and extracts messages_raw to populate the database
     *
     * @param helper Message database helper used to insert messages_raw into the database
     */
    public ParserXMLToDB(MessagesDBHelper helper){
        mDBHelper = helper;
    }


    public void parseXML(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlResourceParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readMessageSets(parser);
        } finally {
            in.close();
        }
    }


    public void parseXML(XmlResourceParser xrp) throws XmlPullParserException, IOException {
        try {
            readMessageSets(xrp);
        }finally {
            xrp.close();
        }
    }


    /**
     * Parses the root element processing Sets
     *
     * @param parser Input parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readMessageSets(XmlPullParser parser) throws XmlPullParserException, IOException {
        // Skip all Start Document elements at the beginning of the XML document.
        while(parser.getEventType()==XmlPullParser.START_DOCUMENT){
            parser.next();
        }
        // Ensure the XML root object is the correct type
        parser.require(XmlPullParser.START_TAG, ns, TAG_MESSAGE_SETS);
        // Parse the MessageSets element until its end tag
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the MessageSets tag
            if (name.equals(TAG_SET)){
                readSet(parser);
            } else {
                skip(parser);
            }
        }
    }

    /**
     * Parses sets. Extracts language and processes all messages_raw in the set.
     *
     * @param parser Input parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readSet(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, TAG_SET);
        // Get the language of the Message set
        String language = parser.getAttributeValue(ns,ATT_LANGUAGE);
        // If the language is not set skip this set, language is necessary.
        if(language==null){
            return;
        }
        // Go through each message element and process it
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the MessageSets tag
            if (name.equals(TAG_MESSAGE)){
                mDBHelper.insertMessage(language, readMessage(parser));
            } else {
                skip(parser);
            }
        }
    }


    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
    // off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private Message readMessage(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, TAG_MESSAGE);
        String code  = parser.getAttributeValue(ns, TAG_CODE);
        // If the code attribute is not set skip this set, language is necessary.
        if(code==null){
            return null;
        }

        String title = null;
        String body  = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            switch (name){
                case TAG_TITLE:
                    title = readMessageElement(parser,TAG_TITLE);
                    break;
                case TAG_BODY:
                    body = readMessageElement(parser,TAG_BODY);
                    break;
                default:
                    skip(parser);
            }
        }

        return new Message(code,title,body);
    }

    // Processes title tags in the feed.
    private String readMessageElement(XmlPullParser parser, String tagName)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tagName);
        String value = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tagName);
        return value;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                    depth--;
                    break;
            case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
