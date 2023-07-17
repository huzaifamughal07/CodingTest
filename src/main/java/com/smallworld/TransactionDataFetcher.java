package com.smallworld;

import com.smallworld.data.Transaction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.*;

public class TransactionDataFetcher {

    /**
     * Returns the sum of the amounts of all transactions
     */
    public double getTotalTransactionAmount(Transaction[] transactions) {
    	
    	  double sum = Arrays.stream(transactions)
                  .mapToDouble(Transaction::getAmount)
                  .sum();
    	return sum;
    }

    /**
     * Returns the sum of the amounts of all transactions sent by the specified client
     */
    public double getTotalTransactionAmountSentBy(String senderFullName, Transaction[] transactions) {
    	 
    	double sum = Arrays.stream(transactions)
                .filter(transaction -> transaction.getSenderFullName().equalsIgnoreCase(senderFullName))
                .mapToDouble(Transaction::getAmount)
                .sum();

         return sum;
    }

    /**
     * Returns the highest transaction amount
     */
    public double getMaxTransactionAmount(Transaction[] transactions) {
    	 double highestAmount = Arrays.stream(transactions)
                 .mapToDouble(Transaction::getAmount)
                 .max()
                 .orElse(0.0);
    	 return highestAmount;
    }

    /**
     * Counts the number of unique clients that sent or received a transaction
     */
    public long countUniqueClients(Transaction[] transactions) {
    	 List<String> uniqueClients = new ArrayList<>();
    	 List<Integer> uniqueMTNs =  new ArrayList<>();
         for (Transaction transaction : transactions) {
             uniqueClients.add(transaction.getSenderFullName());
             uniqueClients.add(transaction.getBeneficiaryFullName());
         }
         for (Transaction transaction : transactions) {
             uniqueMTNs.add(transaction.getMtn());
         }
         
         return uniqueMTNs.size();
    }

    /**
     * Returns whether a client (sender or beneficiary) has at least one transaction with a compliance
     * issue that has not been solved
     */
    public void hasOpenComplianceIssues(String clientFullName, Transaction[] transactions) {
    	// String clientName = "Tom Shelby"; // Replace with the desired client's full name
         boolean hasUnsolvedIssue = false;

         for (Transaction transaction : transactions) {
             if (transaction.getSenderFullName().equalsIgnoreCase(clientFullName)
                     || transaction.getBeneficiaryFullName().equalsIgnoreCase(clientFullName)) {
                 if (!transaction.isIssueSolved()) {
                     hasUnsolvedIssue = true;
                     break;
                 }
             }
         }
         
         if (hasUnsolvedIssue) {
             System.out.println("Client " + clientFullName + " has at least one unsolved compliance issue.");
         } else {
             System.out.println("Client " + clientFullName + " does not have any unsolved compliance issues.");
         }
         
    }

    /**
     * Returns all transactions indexed by beneficiary name
     */
    public Map<String, List<Transaction>> getTransactionsByBeneficiaryName(Transaction[] transactions) {
    	 // Create a map to store transactions indexed by beneficiary name
        Map<String, List<Transaction>> transactionsByBeneficiary = new HashMap<>();

        // Index transactions by beneficiary name
        for (Transaction transaction : transactions) {
            String beneficiaryName = transaction.getBeneficiaryFullName();

            if (!transactionsByBeneficiary.containsKey(beneficiaryName)) {
                transactionsByBeneficiary.put(beneficiaryName, new ArrayList<>());
            }

            transactionsByBeneficiary.get(beneficiaryName).add(transaction);
        }

               
        return transactionsByBeneficiary;
    }

    /**
     * Returns the identifiers of all open compliance issues
     */
    public List<Integer> getUnsolvedIssueIds(Transaction[] transactions) {
    	 List<Integer> openIssueIds = new ArrayList<>();
         for (Transaction transaction : transactions) {
             if (!transaction.isIssueSolved() && transaction.getIssueId() != null) {
                 openIssueIds.add(transaction.getIssueId());
             }
         }

         return openIssueIds;
    }

    /**
     * Returns a list of all solved issue messages
     */
    public List<String> getAllSolvedIssueMessages(Transaction[] transactions) {
    	 List<String> solvedIssueMessages = new ArrayList<>();
         for (Transaction transaction : transactions) {
             if (transaction.isIssueSolved() && transaction.getIssueMessage() != null) {
                 solvedIssueMessages.add(transaction.getIssueMessage());
             }
         }
         
         return solvedIssueMessages;

        
    }

    /**
     * Returns the 3 transactions with the highest amount sorted by amount descending
     */
    public Transaction[]  getTop3TransactionsByAmount(Transaction[] transactions) {
 	
    	// Sort transactions array in descending order based on amount
        Arrays.sort(transactions, Comparator.comparingDouble(Transaction::getAmount).reversed());

        // Retrieve the first three transactions with the highest amount
        Transaction[] highestTransactions = Arrays.copyOfRange(transactions, 0, Math.min(3, transactions.length));
        
        return highestTransactions;      
    }

    /**
     * Returns the senderFullName of the sender with the most total sent amount
     */
    public void getTopSender(Transaction[] transactions) {
    	
    	// Map to store the total sent amount for each sender
    	Map<String, Double> senderTotalAmounts = new HashMap<>();

    	// Calculate the total sent amount for each sender using a for-loop
    	for (Transaction transaction : transactions) {
    	    String senderFullName = transaction.getSenderFullName();
    	    double amount = transaction.getAmount();

    	    // Update the total sent amount for the sender
    	    if (senderTotalAmounts.containsKey(senderFullName)) {
    	        double currentTotalAmount = senderTotalAmounts.get(senderFullName);
    	        senderTotalAmounts.put(senderFullName, currentTotalAmount + amount);
    	    } else {
    	        senderTotalAmounts.put(senderFullName, amount);
    	    }
    	}

    	// Find the sender with the highest total amount using another for-loop
    	String senderWithHighestAmount = null;
    	double highestTotalAmount = 0.0;

    	for (Map.Entry<String, Double> entry : senderTotalAmounts.entrySet()) {
    	    String senderFullName = entry.getKey();
    	    double totalAmount = entry.getValue();

    	    // Update the sender with the highest total amount if necessary
    	    if (totalAmount > highestTotalAmount) {
    	        highestTotalAmount = totalAmount;
    	        senderWithHighestAmount = senderFullName;
    	    }
    	}

    	// Print the sender with the highest total amount
    	if (senderWithHighestAmount != null) {
    	    System.out.println("Sender with the highest total amount: " + senderWithHighestAmount);
    	} else {
    	    System.out.println("No transactions found.");
    	}
    }
    
}

    class Main{
    	@SuppressWarnings("unchecked")
        public static void main (String [] args) { 		
    		ObjectMapper objectMapper = new ObjectMapper();
    		TransactionDataFetcher t1 = new TransactionDataFetcher();

            try {
                // Read JSON data from file
                File file = new File("transactions.json");
                Transaction[] transactions = objectMapper.readValue(file, Transaction[].class);
                
                
                // calling getTotalTransactionAmount() to get Transaction Amount
                double amount= t1.getTotalTransactionAmount(transactions);
                System.out.println("Transaction Amount = "+ amount);
                
                // calling getTotalTransactionAmountSentBy() to get Transaction Amount by specified client
                System.out.println("Transaction Amount by specified client = "+ t1.getTotalTransactionAmountSentBy("tom Shelby", transactions));

                // calling getMaxTransactionAmount() to get maximum Transaction Amount
                System.out.println("Max Transaction Amount = "+ t1.getMaxTransactionAmount(transactions));

                // calling countUniqueClients() to get number of unique client
                System.out.println("No. of Unique Clients = "+ t1.countUniqueClients(transactions));

                // calling hasOpenComplianceIssues() to get if transaction has not been resolved by specified client
                t1.hasOpenComplianceIssues("Aunt polly", transactions);
              
                // calling getTransactionsByBeneficiaryName() to get Transaction Amount indexed by beneficiary name
                Map<String, List<Transaction>> resultTransactionsByBeneficiary = t1.getTransactionsByBeneficiaryName(transactions);
                // Print transactions indexed by beneficiary name
                for (String beneficiaryName : resultTransactionsByBeneficiary.keySet()) {
                    System.out.println("Beneficiary: " + beneficiaryName);
                    List<Transaction> beneficiaryTransactions = resultTransactionsByBeneficiary.get(beneficiaryName);

                    for (Transaction transaction : beneficiaryTransactions) {
                        System.out.println("  MTN: " + transaction.getMtn());
                        System.out.println("  Amount: " + transaction.getAmount());
                        // Print other transaction details as needed
                    }

                    System.out.println();
                }

                

                // calling getUnsolvedIssueIds() to get all open compliance issues
                List<Integer> resultOpenIssueIds = new ArrayList<>();
                resultOpenIssueIds =  t1.getUnsolvedIssueIds(transactions);
                // Print the identifiers of open compliance issues
                System.out.println("Issue ids of open compliance issues: " + resultOpenIssueIds);
                
                

                // calling getAllSolvedIssueMessages() to get list of all solved issue messages
                List<String> resultSolvedIssueMessages = new ArrayList<>();
                resultSolvedIssueMessages = t1.getAllSolvedIssueMessages(transactions);
                
                // Print the list of solved issue messages
                System.out.println("List of solved issue messages:");
                for (String message : resultSolvedIssueMessages) {
                    System.out.println("- " + message);
                }
                
              
                // calling getTop3TransactionsByAmount() to get 3 transactions with the highest amount sorted by amount descending
                Transaction[] highestTransactions = t1.getTop3TransactionsByAmount(transactions);
                
                // Print the highest transactions
                for (Transaction transaction : highestTransactions) {
                    System.out.println("Amount: " + transaction.getAmount());
                }
                
                
                // calling getTopSender() to get the senderFullName of the sender with the most total sent amount
                t1.getTopSender(transactions);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        }
    
    

