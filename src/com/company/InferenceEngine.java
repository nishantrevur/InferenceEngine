package com.company;

import java.io.*;
import java.util.*;

public class InferenceEngine {
    private FileOutputStream out;
    private int noOfQueries;
    private String[] queries;
    private String[] kb;
    private HashMap<String, ArrayList<String>> kbMap = new HashMap<>();
    private int resolutionSteps = 0;

    public void startInference() {
        try {
            getInput();
            standardize();
            makeQueries();
            System.out.println(kbMap);
        }catch (Exception e)
        {

        }
    }

    private void getInput() {
        try {
            int noOfSents;
            FileInputStream in;
            String input = "/Users/nishantrevur/Java Code/AI Assignment/Assignment 3/input_phd.txt";
            in = new FileInputStream(input);
            out = new FileOutputStream("/Users/nishantrevur/Java Code/AI Assignment/Assignment 3/output.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            noOfQueries = Integer.parseInt(br.readLine());
            queries = new String[noOfQueries];
            for (int i = 0; i < noOfQueries; i++) {
                queries[i] = br.readLine();
                //queries[i] = queries[i].replace(" ","");
                //System.out.println(queries[i]);
            }
            noOfSents = Integer.parseInt(br.readLine());
            kb = new String[noOfSents];
            for (int i = 0; i < noOfSents; i++) {
                kb[i] = br.readLine();
                //kb[i] = kb[i].replace(" ","");
                //System.out.println(kb[i]);
            }
            //System.out.println("--------");
        } catch (Exception e) {
            System.out.println("Caught"+e);
        }
    }

    private void standardize() {
        String temp[];
        String pred = "";
        String parr;
        String indParr[];
        String finalPred;
        String finalParr = "";
        String pr = "(";
        String t;
        ArrayList<String> t2;
        HashSet<String> uniqPred = new HashSet<>();
        Iterator<String> itr;
        for (int i = 0; i < kb.length; i++) {
            finalPred = "";
            temp = kb[i].split(" \\| ");
            for (int j = 0; j < temp.length; j++) {
                System.out.println(temp[j]);
                finalParr = "";
                pred = temp[j].substring(0, temp[j].indexOf(pr));
                uniqPred.add(pred);
                parr = temp[j].substring(temp[j].indexOf(pr) + 1, temp[j].length() - 1);
                indParr = parr.split(",");
                for (int k = 0; k < indParr.length; k++) {
                    if (indParr[k].charAt(0) >= 'a' && indParr[k].charAt(0) <= 'z' || indParr[k].charAt(0) == '(' && indParr[k].charAt(1) >= 'a' && indParr[k].charAt(1) <= 'z') {
                        indParr[k] += (i + 1);
                        finalParr += indParr[k];
                        if (k != indParr.length - 1)
                            finalParr += ",";
                        //System.out.println(indParr[k]);
                    } else {
                        finalParr += indParr[k];
                        if (k != indParr.length - 1)
                            finalParr += ",";
                    }
                }
                finalPred += pred + "(" + finalParr + ")";
                if (j != temp.length - 1)
                    finalPred += "|";
            }
            itr = uniqPred.iterator();
            //System.out.println("Final predicate: "+finalPred);
            //System.out.println("Swntence: "+kb[i]+"  "+uniqPred);
            while (itr.hasNext()) {
                t = itr.next();
                //System.out.println("Entered here");
                if (kbMap.containsKey(t)) {
                    t2 = kbMap.get(t);
                    t2.add(finalPred);
                } else {
                    t2 = new ArrayList<String>();
                    t2.add(finalPred);
                    kbMap.put(t, t2);
                    //System.out.println();
                }
            }
            uniqPred.clear();
            //System.out.println("Final: "+finalPred);
        }
        //System.out.println("---------");
        //System.out.println(kbMap);
    }

    private void makeQueries() throws Exception {
       Boolean result;
        for (int i = 0; i < noOfQueries; i++) {
            Stack<String> q = new Stack<String>();
            if (queries[i].charAt(0) != '~')
                queries[i] = "~" + queries[i];
            else
                queries[i] = queries[i].substring(1);
            q.add(queries[i]);
            //kbMap.get(getPredicate(queries[i])).add(queries[i]);
            resolutionSteps=0;
            try {
                result = resolution(q);
            }
            catch (StackOverflowError e)
            {
                result = false;
            }
            System.out.println("Result: "+result);
            out.write((result.toString()+"\n").getBytes());
        }
    }

    private String[] getParameters(String q) {
        int i = 0;
        while (q.charAt(i) != '(') {
            i++;
        }
        return q.substring(i + 1, q.length() - 1).split(",");
    }

    private String getPredicate(String q) {
        return q.substring(0, q.indexOf("("));
    }

    private String removePredicateKB(String pred, String s) {
        if (pred.charAt(0) == '~')
            pred = pred.substring(1);
        else
            pred = "~" + pred;
        int index = s.indexOf(pred);
        if (index == 0 && pred.length() != s.length()) {
            s = s.substring(s.indexOf("|") + 1);
        } else if (pred.equals(s)) {
            return "";
        } else {
            s = s.replace(("|") + pred, "");
        }
        return s;
    }

    private String removePredicateQuery(String pred, String s) {
        int index = s.indexOf(pred);
        if (index == 0 && pred.length() != s.length()) {
            s = s.substring(s.indexOf("|") + 1);
        } else if (pred.equals(s)) {
            return "";
        } else {
            s = s.replace(("|") + pred, "");
        }
        return s;
    }

    private HashMap<String, String> unification(String queryPar[], String[] kbPar) {
        HashMap<String, String> unificationParams = new HashMap<>();
        //System.out.println("querypar: "+queryPar.length+" kbPar: "+kbPar.length);
        Boolean isEqual = true;
        for (int i = 0; i < queryPar.length; i++) {
            if (variable(queryPar[i]) && variable(kbPar[i]))
                unificationParams.put(kbPar[i], queryPar[i]);

            else if (!variable(queryPar[i]) && variable(kbPar[i]))
                unificationParams.put(kbPar[i], queryPar[i]);

            else if (variable(queryPar[i]) && !variable(kbPar[i]))
                unificationParams.put(queryPar[i], kbPar[i]);

            else if (!variable(queryPar[i]) && !variable(kbPar[i])) {
                if (queryPar[i].equals(kbPar[i])) {
                    unificationParams.put(queryPar[i], kbPar[i]);
                } else
                    isEqual = false;
            }
            /*if(!unificationParams.containsKey(kbPar[i]))
            {
                unificationParams.put(kbPar[i],queryPar[i]);
            }*/
        }
        if (!isEqual) {
            //System.out.println("Returned null");
            unificationParams.clear();
            return unificationParams;
        }
        return unificationParams;
    }

    private Boolean variable(String q) {
        return (q.charAt(0) >= 'a' && q.charAt(0) <= 'z');
    }

    private Boolean resolution(Stack<String> qStack) throws StackOverflowError {
        String currentQuery;
        String currentQueryParams[];
        String currentKBSentence;
        String currentKBSentenceParams[];
        String currentKBPredicates[];
        ArrayList<String> currentMatchingPredicate = new ArrayList<>();
        String kbPredicate = "";
        String subParams[];
        String finalSentence = "";
        String finalParams = "";
        HashMap<String, String> unificationParams;
        HashSet<String> nextCall = new HashSet<>();
        System.out.println("Resolving");
        if (qStack.size() != 0) {
            currentQuery = qStack.pop();
            //System.out.println("Entered");
            //currentQueryPredicate = currentQuery.split("\\|");
            System.out.println("the popped element is " + currentQuery);
            //for(int pre=0;pre<currentQueryPredicate.length;pre++) {
            currentQueryParams = getParameters(currentQuery);
            String t2 = currentQuery;
            //System.out.println("Entered here");
            if (t2.charAt(0) == '~')
                t2 = t2.substring(1);
            else
                t2 = "~" + t2;
            System.out.println("Contains: "+kbMap.containsKey(getPredicate(t2))+ "Looking for: "+getPredicate(t2));
            if (kbMap.containsKey(getPredicate(t2))) {
                //System.out.println("here");
                ArrayList<String> kbSentencesWithPredicate = kbMap.get(getPredicate(t2));
                System.out.println(kbSentencesWithPredicate);
                //trying all sentences with predicate
                System.out.println(kbSentencesWithPredicate);
                for (int i = 0; i < kbSentencesWithPredicate.size(); i++)   //values.get
                {
                    System.out.println("Resolution steps: "+resolutionSteps);
                    if (resolutionSteps > 1000) {
                        return false;
                    }
                    currentKBSentence = kbSentencesWithPredicate.get(i);
                    currentKBPredicates = currentKBSentence.split("\\|");
                    System.out.println("Current kb sentence " + currentKBSentence + " Values: "+kbSentencesWithPredicate.size()+" i: "+i);
                    //looking for matching predicate
                    currentMatchingPredicate.clear();
                    for (int m = 0; m < currentKBPredicates.length; m++) {
                        if (getPredicate(t2).equals(getPredicate(currentKBPredicates[m]))) {
                            currentMatchingPredicate.add(currentKBPredicates[m]);
                        }
                    }
                    Stack<String> tempStack = new Stack<>();
                    tempStack.addAll(qStack);
                    for (int mat = 0; mat < currentMatchingPredicate.size(); mat++)
                    {
                        qStack.removeAllElements();
                        qStack.addAll(tempStack);
                        currentKBSentenceParams = getParameters(currentMatchingPredicate.get(mat));
                        System.out.println("Unifying q: " + t2 + "  k: " + currentMatchingPredicate.get(mat));
                        unificationParams = unification(currentQueryParams, currentKBSentenceParams);
                        //System.out.println(unificationParams);
                        if (unificationParams.size() != 0) {
                            for (int p = 0; p < currentKBPredicates.length; p++)   //ored
                            {
                                kbPredicate = currentKBPredicates[p];
                                finalParams = "";
                                finalSentence = "";
                                subParams = getParameters(kbPredicate);
                                System.out.println("currentkbelement before sub " + kbPredicate+" p: "+p);
                                for (int par = 0; par < subParams.length; par++)
                                {
                                    if (unificationParams.get(subParams[par]) != null)
                                        subParams[par] = unificationParams.get(subParams[par]);
                                    finalParams += subParams[par];
                                    if (par != subParams.length - 1)
                                        finalParams += ",";
                                }
                                finalSentence += getPredicate(currentKBPredicates[p]) + "(" + finalParams + ")";
                                kbPredicate = finalSentence;
                                System.out.println("after substitution: " + kbPredicate);
                                System.out.println("checking " + getPredicate(kbPredicate));
                                if (!getPredicate(kbPredicate).equals(getPredicate(t2)))
                                {
                                    System.out.println("Current kbeleemnt end: ---" + kbPredicate);
                                    String temp = kbPredicate;
                                    Boolean isRemoved = false;
                                    Iterator<String> s = qStack.iterator();
                                    nextCall.clear();
                                    if (temp.charAt(0) == '~')
                                        temp = temp.substring(1);
                                    else
                                        temp = "~" + temp;
                                    Boolean allConstants=true;
                                    while(s.hasNext())
                                    {
                                        if(s.next().equals(temp))
                                        {
                                            s.remove();
                                            isRemoved = true;
                                        }
                                    }
                                    /*if(!isRemoved)
                                    {
                                        qStack.push(kbPredicate);
                                    }*/
                                    if (!isRemoved) {
                                        System.out.println("Pushed: "+kbPredicate);
                                        qStack.add(kbPredicate);
                                    }
                                }
                            }//ored
                            System.out.println("---------------");
                            if(!qStack.empty())
                                //System.out.println("Stack: "+qStack.peek());
                            resolutionSteps++;
                            Boolean result = resolution(qStack);
                            if (result) {
                                //System.out.println("true");
                                return true;
                            }
                        }//uniparams
                    }//loop
                }//values
                System.out.println("false1");
                resolutionSteps--;
                return false;
            } else
                System.out.println("false2");
            resolutionSteps--;
                return false;
        }
        return true;
    }
}
