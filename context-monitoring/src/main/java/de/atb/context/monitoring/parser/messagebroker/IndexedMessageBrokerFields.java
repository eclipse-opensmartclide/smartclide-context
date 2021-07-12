package de.atb.context.monitoring.parser.messagebroker;

/*
 * #%L
 * ATB Context Monitoring Core Services
 * %%
 * Copyright (C) 2015 - 2021 ATB
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */


import de.atb.context.monitoring.index.IFieldable;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;

/**
 * Fields
 * 
 * @author scholze
 * @version $LastChangedRevision: 143 $
 * 
 */
public enum IndexedMessageBrokerFields implements IFieldable {

	Port("port"),

	Username("username"),

	Password("password"),

	Topic("topic"),

	Content("content"),

    Uri("uri"),

	;

	private final String name;

	IndexedMessageBrokerFields(final String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IFieldable#get(org.apache.lucene
	 * .document.Document)
	 */
	@Override
	public Field get(final Document document) {
        return (Field) document.getField(this.name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IFieldable#getString(org.apache.
	 * lucene.document.Document)
	 */
	@Override
	public String getString(final Document document) {
		return get(document).stringValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IFieldable#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IFieldable#create(java.lang.String)
	 */
	@Override
	public Field createField(final String value) {
        return new Field(getName(), value, StringField.TYPE_STORED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IFieldable#create(java.lang.String,
	 * org.apache.lucene.document.Field.Store,
	 * org.apache.lucene.document.Field.Index)
	 */
	@Override
	public Field createField(final String value, final Store store) {
		return new Field(getName(), value, StringField.TYPE_STORED);
	}
}
