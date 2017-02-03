package org.opennms.vaadin.provision.model;

public class SyncOperationNode {

		private String NODE;
		private BasicNode.OnmsState STATUS;
		private boolean SYNCTRUE=false;
		private boolean SYNCFALSE=false;
		private boolean SYNCDBONLY=false;
		
		public SyncOperationNode(String nodelabel, BasicNode.OnmsState status, BasicNode.OnmsSync sync) {
			NODE=nodelabel;
			STATUS=status;
			if ( sync == BasicNode.OnmsSync.DBONLY)
				SYNCDBONLY=true;
			else if ( sync == BasicNode.OnmsSync.TRUE)
				SYNCTRUE=true;
			else if (sync == BasicNode.OnmsSync.FALSE)
				SYNCFALSE=true;
		}
		
		public SyncOperationNode(BasicNode node) {
			NODE=node.getNodeLabel();
			STATUS=node.getOnmstate();
			
			if (node.getSyncOperations().contains(BasicNode.OnmsSync.DBONLY))
				SYNCDBONLY=true;

			if (node.getSyncOperations().contains(BasicNode.OnmsSync.TRUE))
				SYNCTRUE=true;
			
			if (node.getSyncOperations().contains(BasicNode.OnmsSync.FALSE))
				SYNCFALSE=true;
		}

		public String getNODE() {
			return NODE;
		}
		
		public boolean isSYNCTRUE() {
			return SYNCTRUE;
		}

		public boolean isSYNCFALSE() {
			return SYNCFALSE;
		}

		public boolean isSYNCDBONLY() {
			return SYNCDBONLY;
		}

		public BasicNode.OnmsState getSTATUS() {
			return STATUS;
		}
				


}
