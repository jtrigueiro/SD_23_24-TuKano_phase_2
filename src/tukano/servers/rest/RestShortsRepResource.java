package tukano.servers.rest;

import java.net.InetAddress;
import java.util.List;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.Provider;
import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.rest.RestShorts;
import tukano.servers.java.ShortsServer;
import tukano.utils.kafka.lib.KafkaPublisher;
import tukano.utils.kafka.lib.KafkaSubscriber;
import tukano.utils.kafka.lib.RecordProcessor;
import tukano.utils.kafka.sync.SyncPoint;

@Singleton
@Provider
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RestShortsRepResource extends RestResource implements RestShorts, RecordProcessor {

	final Shorts impl;

	static final String FROM_BEGINNING = "earliest";
	static final String TOPIC = "single_partition_topic";
	static final String KAFKA_BROKERS = "kafka:9092";

	private static final String CREATESHORT = "createShort";
	private static final String DELETESHORT = "deleteShort";
	private static final String FOLLOW = "follow";
	private static final String LIKE = "like";
	private static final String DELETEUSERSHORTS = "deleteUserShorts";

	private static Logger Log = Logger.getLogger(RestShortsRepResource.class.getName());

	final String replicaId;
	final KafkaPublisher sender;
	final KafkaSubscriber receiver;
	final SyncPoint<Result> sync;

	public RestShortsRepResource() {
		try {
			this.replicaId = InetAddress.getLocalHost().getHostName();
			this.sender = KafkaPublisher.createPublisher(KAFKA_BROKERS);
			this.receiver = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), FROM_BEGINNING);
			this.receiver.start(false, this);
			this.sync = new SyncPoint<>();

			this.impl = new ShortsServer();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onReceive(ConsumerRecord<String, String> r) {
		Result result = Result.ok();

		try {
			String method = r.key();
			String[] param = r.value().split(",");
			switch (method) {
				case CREATESHORT:
					super.resultOrThrow(PreConditionsShorts.createShort(param[0], param[1]));
					result = Result.ok(
							PosOperationShorts.createShort(param[2], param[0], param[3], Long.parseLong(param[4]),
									Integer.parseInt(param[5])));
					break;
				case DELETESHORT:
					result = impl.deleteShort(param[0], param[1]);
					break;
				case FOLLOW:
					result = impl.follow(param[0], param[1], Boolean.parseBoolean(param[2]), param[3]);
					break;
				case LIKE:
					result = impl.like(param[0], param[1], Boolean.parseBoolean(param[2]), param[3]);
					break;
				case DELETEUSERSHORTS:
					result = impl.deleteUserShorts(param[0], param[1]);
					break;
				default:
					Log.severe("No such command\n");
			}
			sync.setResult(r.offset(), result);

		} catch (Exception e) {
			Log.severe("Exception in OnReceive function\n");
		}

	}

	@Override
	public Short createShort(long headerVer, String userId, String password) {
		Log.info("Received createShort; Version: " + headerVer + ")");

		Short s = impl.createShort(userId, password).value();
		impl.deleteShort(s.getShortId(), password);

		String[] urls = s.getBlobUrl().split("\\?");
		String firstUrl = urls[0];
		String secondUrl = urls[1].split("|")[1];
		String cleanUrls = firstUrl + "|" + secondUrl;

		var version = sender.publish(TOPIC, CREATESHORT, userId + "," + password + "," + s.getShortId() + ","
				+ cleanUrls + "," + s.getTimestamp() + "," + s.getTotalLikes());
		Result<Short> result = sync.waitForResult(version);
		return result.value();
	}

	@Override
	public void deleteShort(long headerVer, String shortId, String password) {
		var version = sender.publish(TOPIC, DELETESHORT, shortId + "," + password);
		Result<Void> result = sync.waitForResult(version);
		super.resultOrThrow(result);
	}

	@Override
	public Short getShort(long headerVer, String shortId) {
		Result<Short> result = sync.waitForResult(headerVer);
		return super.resultOrThrow(result);
	}

	@Override
	public List<String> getShorts(long headerVer, String userId) {
		Result<List<String>> result = sync.waitForResult(headerVer);
		return super.resultOrThrow(result);
	}

	@Override
	public void follow(long headerVer, String userId1, String userId2, boolean isFollowing, String password) {
		var version = sender.publish(TOPIC, FOLLOW, userId1 + "," + userId2 + "," + isFollowing + "," + password);
		Result<Void> result = sync.waitForResult(version);
		super.resultOrThrow(result);
	}

	@Override
	public List<String> followers(long headerVer, String userId, String password) {
		Result<List<String>> result = sync.waitForResult(headerVer);
		return super.resultOrThrow(result);
	}

	@Override
	public void like(long headerVer, String shortId, String userId, boolean isLiked, String password) {
		var version = sender.publish(TOPIC, LIKE, shortId + "," + userId + "," + isLiked + "," + password);
		Result<Void> result = sync.waitForResult(version);
		super.resultOrThrow(result);
	}

	@Override
	public List<String> likes(long headerVer, String shortId, String password) {
		Result<List<String>> result = sync.waitForResult(headerVer);
		return super.resultOrThrow(result);
	}

	@Override
	public List<String> getFeed(long headerVer, String userId, String password) {
		Result<List<String>> result = sync.waitForResult(headerVer);
		return super.resultOrThrow(result);
	}

	@Override
	public void deleteUserShorts(long headerVer, String userId, String token) {
		long version = sender.publish(TOPIC, DELETEUSERSHORTS, userId + "," + token);
		Result<Void> result = sync.waitForResult(version);
		super.resultOrThrow(result);
	}

}