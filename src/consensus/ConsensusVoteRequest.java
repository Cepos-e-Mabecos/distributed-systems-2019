package consensus;

import java.io.Serializable;

public class ConsensusVoteRequest implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 4139517296885376205L;
  
  // Attributes
  private Integer candidateTerm;
  private String candidateAddress;
  private Integer candidatePort;
  
  // Constructor
  public ConsensusVoteRequest(Integer candidateTerm, String candidateAddress, Integer candidatePort) {
    this.candidateTerm = candidateTerm;
    this.candidateAddress = candidateAddress;
    this.candidatePort = candidatePort;
  }

  // Getters & Setters
  public Integer getCandidateTerm() {
    return candidateTerm;
  }

  public void setCandidateTerm(Integer candidateTerm) {
    this.candidateTerm = candidateTerm;
  }

  public String getCandidateAddress() {
    return candidateAddress;
  }

  public void setCandidateAddress(String candidateAddress) {
    this.candidateAddress = candidateAddress;
  }
  
  public Integer getCandidatePort() {
    return candidatePort;
  }
  
  public void setCandidatePort(Integer candidatePort) {
    this.candidatePort = candidatePort;
  }
}
