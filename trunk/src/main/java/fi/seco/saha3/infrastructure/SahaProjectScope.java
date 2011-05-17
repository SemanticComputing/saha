package fi.seco.saha3.infrastructure;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.Scope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import fi.seco.saha3.web.control.ASahaController;

public class SahaProjectScope implements Scope {

	private SahaProjectRegistry spr;

	@Required
	public void setSahaProjectRegistry(SahaProjectRegistry spr) {
		this.spr = spr;
	}

	private String getSahaProjectName() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		return ASahaController.parseModelName(attributes.getRequest().getRequestURI());
	}

	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		if ("scopedTarget.ScopedModel".equals(name))
			return spr.getModel(getSahaProjectName());
		else if ("scopedTarget.ScopedProject".equals(name)) return spr.getSahaProject(getSahaProjectName(), true);
		throw new UnsupportedOperationException("Bean name needs to be either ScopedModel or ScopedProject: " + name);
	}

	@Override
	public Object remove(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {}

	@Override
	public String getConversationId() {
		return null;
	}

	@Override
	public Object resolveContextualObject(String key) {
		return null;
	}

}
