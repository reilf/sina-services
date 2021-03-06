package cc.pp.sina.solr.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.common.SolrInputDocument;

import cc.pp.sina.dao.common.MybatisConfig;
import cc.pp.sina.dao.friends.SinaFriendsDis;
import cc.pp.sina.domain.friends.FriendsInfo;
import cc.pp.sina.domain.users.UserInfo;

public class IndexSinaUsersRun implements Runnable {

	//	private static Logger logger = LoggerFactory.getLogger(IndexSinaUsers.class);

	/**
	 * 新浪用户关注数据获取
	 */
	private static final String SINA_USER_FRIENDS = "sina_user_friends_";
	private static SinaFriendsDis sinaFriends1 = new SinaFriendsDis(MybatisConfig.ServerEnum.friend1);
	private static SinaFriendsDis sinaFriends2 = new SinaFriendsDis(MybatisConfig.ServerEnum.friend2);

	private final CloudSolrServer cloudServer;
	private final List<UserInfo> users;

	public IndexSinaUsersRun(CloudSolrServer cloudServer, List<UserInfo> users) {
		this.cloudServer = cloudServer;
		this.users = users;
	}

	@Override
	public void run() {

		Collection<SolrInputDocument> docs = new ArrayList<>();
		for (UserInfo user : users) {
			docs.add(getDoc(user));
		}
		try {
			cloudServer.add(docs);
			cloudServer.commit();
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}

	}

	private SolrInputDocument getDoc(UserInfo user) {

		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", user.getId());
		doc.addField("username", user.getId());
		doc.addField("screenname", user.getScreen_name(), 3.0f);
		doc.addField("name", user.getName());
		doc.addField("province", user.getProvince());
		doc.addField("city", user.getCity());
		doc.addField("location", user.getLocation());
		doc.addField("description", user.getDescription(), 2.0f);
		doc.addField("url", user.getUrl());
		doc.addField("profileimageurl", user.getProfile_image_url());
		doc.addField("domain", user.getDomain());
		doc.addField("gender", user.getGender());
		doc.addField("followerscount", user.getFollowers_count());
		doc.addField("friendscount", user.getFriends_count());
		doc.addField("statusescount", user.getStatuses_count());
		doc.addField("favouritescount", user.getFavourites_count());
		doc.addField("createdat", user.getCreated_at());
		doc.addField("verified", user.isVerified());
		doc.addField("verifiedtype", user.getVerified_type());
		doc.addField("avatarlarge", user.getAvatar_large());
		doc.addField("bifollowerscount", user.getBi_followers_count());
		doc.addField("remark", user.getRemark());
		doc.addField("verifiedreason", user.getVerified_reason());
		doc.addField("weihao", user.getWeihao());
		doc.addField("lastime", System.currentTimeMillis() / 1000);

		/**
		 * 注意：friends字段数据要和上面所有字段一起添加
		 */
		long username = user.getId();
		FriendsInfo friendsInfo = null;
		if (username % 64 < 32) {
			friendsInfo = sinaFriends1.getSinaFriendsInfo(SINA_USER_FRIENDS + username % 64, username);
		} else {
			friendsInfo = sinaFriends2.getSinaFriendsInfo(SINA_USER_FRIENDS + username % 64, username);
		}
		if (friendsInfo != null) {
			doc.addField("friends", friendsInfo.getFriendsuids());
		} else {
			doc.addField("friends", "");
		}

		return doc;
	}

}
