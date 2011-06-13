package org.genmapp.genmappimport.reader;

	public enum SupportedFileType {
		EXCEL("xls"), OOXML("xlsx"), TEXT("txt");
		
		private final String extension;
		
		private SupportedFileType(final String extension) {
			this.extension = extension;
		}
		
		/**
		 * 
		 * @return file extension with dot.
		 */
		public String getExtension() {
			return "." + extension;
		}
}
