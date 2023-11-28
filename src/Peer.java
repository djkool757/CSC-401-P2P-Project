class Peer {
        private String hostname;
        private int uploadPort;

        public Peer(String hostname, int uploadPort) {
            this.hostname = hostname;
            this.uploadPort = uploadPort;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public int getUploadPort() {
            return uploadPort;
        }

        public void setUploadPort(int uploadPort) {
            this.uploadPort = uploadPort;
        }
    }