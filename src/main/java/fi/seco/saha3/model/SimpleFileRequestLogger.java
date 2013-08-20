/**
 * 
 */
package fi.seco.saha3.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Locale;

import org.apache.commons.collections15.buffer.CircularFifoBuffer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Required;

public class SimpleFileRequestLogger implements IRequestLogger, DisposableBean {

	private static final String fileName = "request.log";

	private String path;

	private Writer writer;

	private IModelReader mr;

	@Required
	public void setModelReader(IModelReader mr) {
		this.mr = mr;
	}

	@Required
	public void setPath(String path) {
		this.path = path + "/";
		File file = new File(this.path + fileName);
		try {
			writer = new FileWriter(file, true);
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	@Override
	public void setObjectProperty(String remoteHost, String s, String p, UriLabel o) {
		try {
			ISahaResource r = mr.getResource(p, new Locale("fi"));
			ISahaResource r2 = mr.getResource(s, new Locale("fi"));
			writer.append(String.format("%tF %tR %s: set <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a> <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a>: <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a><br />\n", Calendar.getInstance(), Calendar.getInstance(), remoteHost, s, getLabel(r2), p, r.getLabel(), o.getUri(), o.getLabel()));
			writer.flush();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	@Override
	public void createInstance(String remoteHost, String uri, String type, String label) {
		try {
			ISahaResource r = mr.getResource(type, new Locale("fi"));
			writer.append(String.format("%tF %tR %s: created new instance <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a> (<a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a>)<br />\n", Calendar.getInstance(), Calendar.getInstance(), remoteHost, uri, label, type, r.getLabel()));
			writer.flush();
		} catch (IOException e) {
			throw new IOError(e);
		}

	}

	@Override
	public void setLiteralProperty(String remoteHost, String s, String p, UriLabel object) {
		try {
			ISahaResource r = mr.getResource(p, new Locale("fi"));
			ISahaResource r2 = mr.getResource(s, new Locale("fi"));
			writer.append(String.format("%tF %tR %s: set <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a> <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a>: (%s,%s)<br />\n", Calendar.getInstance(), Calendar.getInstance(), remoteHost, s, getLabel(r2), p, r.getLabel(), object.getLabel(), object.getLang()));
			writer.flush();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	@Override
	public void updateLiteralProperty(String remoteHost, String s, String p, UriLabel removed, UriLabel added) {
		try {
			ISahaResource r = mr.getResource(p, new Locale("fi"));
			ISahaResource r2 = mr.getResource(s, new Locale("fi"));
			writer.append(String.format("%tF %tR %s: updated <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a> <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a>: (%s,%s) [was (%s,%s)]<br />\n", Calendar.getInstance(), Calendar.getInstance(), remoteHost, s, getLabel(r2), p, r.getLabel(), added.getLabel(), added.getLang(), removed.getLabel(), removed.getLang()));
			writer.flush();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	@Override
	public void setMapProperty(String remoteHost, String s, String fc, String value) {
		try {
			ISahaResource r = mr.getResource(s, new Locale("fi"));
			writer.append(String.format("%tF %tR %s: updated %s map property of <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a> to %s<br />\n", Calendar.getInstance(), Calendar.getInstance(), remoteHost, fc, s, getLabel(r), value));
			writer.flush();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	@Override
	public void removeLiteralProperty(String remoteHost, String s, String p, UriLabel object) {
		try {
			ISahaResource r = mr.getResource(p, new Locale("fi"));
			ISahaResource r2 = mr.getResource(p, new Locale("fi"));
			writer.append(String.format("%tF %tR %s: removed <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a> <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a>: (%s,%s)<br />\n", Calendar.getInstance(), Calendar.getInstance(), remoteHost, s, getLabel(r2), p, r.getLabel(), object.getLabel(), object.getLang()));
			writer.flush();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	private static String getLabel(ISahaResource r) {
		StringBuilder label = new StringBuilder();
		label.append(r.getLabel());
		if (!r.getTypes().isEmpty()) {
			label.append(" (");
			for (UriLabel ul : r.getTypes()) {
				label.append(ul.getLabel());
				label.append(", ");
			}
			label.setLength(label.length() - 2);
			label.append(')');
		}
		return label.toString();
	}

	@Override
	public void removeResource(String remoteHost, String uri) {
		try {
			ISahaResource r = mr.getResource(uri, new Locale("fi"));
			int refs = r.getInverseProperties().size();
			int props = r.getProperties().size();
			writer.append(String.format("<font color='red'>%tF %tR %s: removed resource <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a> with %,d properties and %,d references</font><br />\n", Calendar.getInstance(), Calendar.getInstance(), remoteHost, uri, getLabel(r), props, refs));
			writer.flush();
		} catch (IOException e) {
			throw new IOError(e);
		}

	}

	@Override
	public void removeObjectProperty(String remoteHost, String s, String p, String o) {
		try {
			ISahaResource r = mr.getResource(s, new Locale("fi"));
			ISahaResource pr = mr.getResource(p, new Locale("fi"));
			ISahaResource r2 = mr.getResource(o, new Locale("fi"));
			writer.append(String.format("%tF %tR %s: removed <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a> <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a>: <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a><br />\n", Calendar.getInstance(), Calendar.getInstance(), remoteHost, s, getLabel(r), p, pr.getLabel(), o, getLabel(r2)));
			writer.flush();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	@Override
	public void removeProperty(String remoteHost, String s, String p) {
		try {
			ISahaResource pr = mr.getResource(p, new Locale("fi"));
			ISahaResource sr = mr.getResource(s, new Locale("fi"));
			writer.append(String.format("%tF %tR %s: removed all <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a> of <a href=\"resource.shtml?model=|MODEL|&uri=%s\">%s</a><br />\n", Calendar.getInstance(), Calendar.getInstance(), remoteHost, p, pr.getLabel(), s, getLabel(sr)));
			writer.flush();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	@Override
	public void destroy() throws Exception {
		writer.close();
	}

	@Override
	public String getLastLog(int lines) {
		CircularFifoBuffer<String> buffer = new CircularFifoBuffer<String>(lines);
		try {
			BufferedReader br = new BufferedReader(new FileReader(path + fileName));
			String line;
			while ((line = br.readLine()) != null)
				buffer.add(line);
			br.close();
		} catch (FileNotFoundException e) {
			throw new IOError(e);
		} catch (IOException e) {
			throw new IOError(e);
		}
		return StringUtils.join(buffer, null);
	}
}
