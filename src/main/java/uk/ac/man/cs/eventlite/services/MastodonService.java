package uk.ac.man.cs.eventlite.services;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.entity.Status.Visibility;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.method.Timelines;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.OkHttpClient;

@Service
public class MastodonService {

	private String accessToken = Dotenv.load().get("MASTODON_ACCESS_TOKEN");

	private String serverURL = "techhub.social";

	private MastodonClient createClient() {
		return new MastodonClient.Builder(serverURL, new OkHttpClient.Builder(), new Gson())
				.accessToken(accessToken)
				.useStreamingApi()
				.build();
	}

	public Status postStatus(String content) throws Mastodon4jRequestException {
		MastodonClient client = createClient();
		Statuses statuses = new Statuses(client);

		return statuses.postStatus(content, null, null, false, null, Visibility.Unlisted).execute();
	}

	public List<Status> getTimeline() {
		try {
			MastodonClient client = createClient();
			Timelines timeline = new Timelines(client);

			// Get home timeline
			return timeline.getHome().execute().getPart();
		} catch (Mastodon4jRequestException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}

	}

}
