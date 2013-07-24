/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.http.service.gae;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.SocketConnection;

public class TestWebServer implements Container {
	public AtomicBoolean fatal = new AtomicBoolean(false);
	private SocketConnection connection;
	private int port = 9999;

	public final void start() throws IOException {
		connection = new SocketConnection(this);
		SocketAddress address = new InetSocketAddress(port);
		connection.connect(address);
	}

	public String getUri() {
		return "http://localhost:" + port;
	}

	public final void stop() throws IOException {
		connection.close();
	}

	@Override
	public void handle(Request req, Response resp) {
		try {
			if ("GET".equalsIgnoreCase(req.getMethod())) {
				get(req, resp);
			}
			if ("POST".equalsIgnoreCase(req.getMethod())) {
				post(req, resp);
			}
			if ("PUT".equalsIgnoreCase(req.getMethod())) {
				put(req, resp);
			}
			if ("DELETE".equalsIgnoreCase(req.getMethod())) {
				delete(req, resp);
			}
			if ("HEAD".equalsIgnoreCase(req.getMethod())) {
				head(req, resp);
			}
			if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
				options(req, resp);
			}
		} catch (Exception e) {
			resp.setCode(500);
			resp.setText(e.getMessage());
		}
		try {
			resp.commit();
			resp.close();
		} catch (Exception e1) {
			e1.printStackTrace();
			fatal.set(true);
		}
	}

	public void get(Request req, Response resp) throws Exception {

	}

	public void post(Request req, Response resp) throws Exception {

	}

	public void put(Request req, Response resp) throws Exception {

	}

	public void delete(Request req, Response resp) throws Exception {

	}

	public void head(Request req, Response resp) throws Exception {

	}

	public void options(Request req, Response resp) throws Exception {

	}
}
