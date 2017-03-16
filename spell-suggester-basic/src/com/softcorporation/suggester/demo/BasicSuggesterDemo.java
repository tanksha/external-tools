/**
 * Copyright (c) 2005 SoftCorporation LLC. All rights reserved.
 *
 * The Software License, Version 1.0
 *
 * SoftCorporation LLC. grants you ("Licensee") a non-exclusive, royalty free,
 * license to use, modify and redistribute this software in source and binary
 * code form, provided that the following conditions are met:
 *
 * 1. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        SoftCorporation LLC. (http://www.softcorporation.com)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 2. The names "Suggester" and "SoftCorporation" must not be used to
 *    promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    info@softcorporation.com.
 *
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.
 * IN NO EVENT SHALL THE SOFTCORPORATION BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION).
 *
 */
package com.softcorporation.suggester.demo;

import java.io.*;
import java.util.*;

import com.softcorporation.suggester.util.Constants;
import com.softcorporation.suggester.util.BasicSuggesterConfiguration;
import com.softcorporation.suggester.dictionary.BasicDictionary;
import com.softcorporation.suggester.BasicSuggester;
import com.softcorporation.suggester.Suggestion;

/**
 * Basic Suggester Demo
 * <p>
 * @version   1.0, 02/02/2005
 * @author    Vadim Permakoff
 */
public class BasicSuggesterDemo
{
  public static void main(String[] args)
  {
    String command;
    String word;

    try
    {
//    Logger.setLogDebug(true);

      long memory0;
      long memory1;

      long time0;
      long time1;

//      String language = "ru";
//      String dictName = "russian.jar";
      String language = "en";
      String dictFileName = "file:/home/tanksha/spell-correct/suggester-basic/dic/english.jar";

      BufferedReader keyboardInput = new BufferedReader(new InputStreamReader(
          System.in, Constants.CHARACTER_SET_ENCODING_DEFAULT));

//DEBUG
//      System.out.println("Hit <Enter> to load dictionary");
//      keyboardInput.readLine();
      System.out.println("Loading dictionary ...");
      memory0 = getMemory();
      time0 = System.currentTimeMillis();

      BasicDictionary dictionary = new BasicDictionary(dictFileName);

//DEBUG
      time1 = System.currentTimeMillis();
      memory1 = getMemory();
      System.out.println("Done. It took " + (time1 - time0) +
                         " milliseconds. Used memory: " + (memory1 - memory0) +
                         "\n");

//DEBUG
//      System.out.println("Hit <Enter> to load configuration");
//      keyboardInput.readLine();

//      BasicSuggesterConfiguration configuration = new
//          BasicSuggesterConfiguration("file://basicSuggester.config");
      BasicSuggesterConfiguration configuration = new
          BasicSuggesterConfiguration("/com/softcorporation/suggester/basicSuggester.config");

      BasicSuggester suggester = new BasicSuggester(configuration);
      suggester.attach(dictionary);

      while (true)
      {
        System.out.print(
            "\nPlease enter single word to search ('Enter' - exit): ");
        word = keyboardInput.readLine();

        System.out.println("\nword: " + word);

        if (word.trim().length() == 0)
        {
          break;
        }

        ArrayList suggestions = null;
        time0 = System.currentTimeMillis();
        suggestions = suggester.getSuggestions(word, 10, language);
        time1 = System.currentTimeMillis();
        System.out.println("Done. It took " + (time1 - time0) +
                           " milliseconds.\n");
        /*
                // better approact to measure time would be to run it many times
                int n = 1000;
                for (int i = 0; i < n; i++)
                {
                  suggestions = suggester.getSuggestions(word, 10, language);
                }
                time1 = System.currentTimeMillis();
                System.out.println("Done. It took " +
                                   ( (float) (time1 - time0)) / n +
                                   " milliseconds.\n");
         */
        for (int j = 0; j < suggestions.size(); j++)
        {
          Suggestion suggestion = (Suggestion) suggestions.get(j);
          System.out.println("word " + (j + 1) + ": " + suggestion.getWord());
        }
        System.out.println("\nTotal found: " + suggestions.size());

        int result = suggester.hasWord(word);
        if (result == Constants.RESULT_ID_MATCH_EXACT)
        {
          System.out.println("Dictionaries: Exact Word");
        }
        else if (result == Constants.RESULT_ID_MATCH)
        {
          System.out.println("Dictionaries: Case Word");
        }
        else
        {
          System.out.println("Dictionaries: No Word.");
        }
      }
      System.out.println("\nExit.");
    }
    catch (Exception e)
    {
      System.out.println("Error: " + e);
    }
  }

  // Note, this is not valid method to measure memory size, but it can give you some estimate
  static long getMemory()
  {
    try
    {
      System.gc();
      System.gc();
      Thread.yield();
      System.gc();
      System.gc();
      Thread.sleep(100);
      System.gc();
      System.gc();
    }
    catch (Exception e)
    {}
//    System.out.println("TotalMemory=" + Runtime.getRuntime().totalMemory());
//    System.out.println("FreeMemory=" + Runtime.getRuntime().freeMemory());
    return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }

}
