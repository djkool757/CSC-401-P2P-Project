/**
 * Represents a Request for Comments (RFC) document.
 */
class RFC {
    private int rfcNumber;
    private String title;
    private String peerHostname;
     /**
     * Constructs an RFC object with the specified number, title, and hostname.
     * 
     * @param number   the RFC number
     * @param title    the RFC title
     * @param hostname the hostname of the server hosting the RFC document
     */
    public RFC(int rfcNumber, String title, String peerHostname) {
        this.rfcNumber = rfcNumber;
        this.title = title;
        this.peerHostname = peerHostname;
    }
    public int getRfcNumber() {
        return rfcNumber;
    }
    public void setRfcNumber(int rfcNumber) {
        this.rfcNumber = rfcNumber;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getPeerHostname() {
        return peerHostname;
    }
    public void setPeerHostname(String peerHostname) {
        this.peerHostname = peerHostname;
    }
}
