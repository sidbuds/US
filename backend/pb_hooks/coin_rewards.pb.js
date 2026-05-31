// ============================================================
// 秘密基地 — 金币奖励 Hook (PocketBase v0.22)
// 自动处理各种行为的金币奖励和每日上限
// ============================================================

var REWARDS = {
  checkin: 5,
  diary: 20,
  like: 2,
  comment: 5,
  expense: 5,
  login: 10,
  miss: 10,
  hug_cost: 5, hug_recv: 15,
  kiss_cost: 5, kiss_recv: 15
};

var DAILY_LIMITS = {
  checkin: 7, diary: 2, like: 10,
  comment: 5, expense: 5
};

function todayStr() {
  var d = new Date();
  return d.getFullYear() + "-" +
    ("0" + (d.getMonth() + 1)).slice(-2) + "-" +
    ("0" + d.getDate()).slice(-2);
}

function countToday(app, spaceId, userId, reason) {
  var today = todayStr();
  try {
    var records = app.dao().findRecordsByFilter(
      "coin_transactions",
      "space_id='" + spaceId + "' && user_id='" + userId + "' && reason='" + reason + "' && created>='" + today + "'",
      "-created", 100, 0
    );
    return records.length;
  } catch (e) { return 0; }
}

function findCoinRecord(app, spaceId) {
  try {
    var records = app.dao().findRecordsByFilter(
      "coins", "space_id='" + spaceId + "'", "", 1, 0
    );
    if (records.length > 0) return records[0];
  } catch (e) { }
  return null;
}

function updateCoins(app, spaceId, delta, isSpend) {
  var coin = findCoinRecord(app, spaceId);
  if (!coin) return;
  var balance = coin.getFloat("balance") || 0;
  var earned = coin.getFloat("total_earned") || 0;
  var spent = coin.getFloat("total_spent") || 0;
  coin.set("balance", balance + delta);
  if (isSpend) {
    coin.set("total_spent", spent + Math.abs(delta));
  } else {
    coin.set("total_earned", earned + delta);
  }
  app.dao().saveRecord(coin);
}

function createTx(app, spaceId, userId, txType, reason, amount, relatedId) {
  var collection = app.dao().findCollectionByNameOrId("coin_transactions");
  var record = new Record(collection);
  record.set("space_id", spaceId);
  record.set("user_id", userId);
  record.set("type", txType);
  record.set("reason", reason);
  record.set("amount", amount);
  record.set("related_id", relatedId || "");
  app.dao().saveRecord(record);
}

function rewardCoins(app, spaceId, userId, reason, amount, dailyLimit, relatedId) {
  if (dailyLimit > 0) {
    var count = countToday(app, spaceId, userId, reason);
    if (count >= dailyLimit) return;
  }
  updateCoins(app, spaceId, amount, false);
  createTx(app, spaceId, userId, "earn", reason, amount, relatedId);
}

function spendCoins(app, spaceId, userId, reason, amount, relatedId) {
  updateCoins(app, spaceId, -amount, true);
  createTx(app, spaceId, userId, "spend", reason, amount, relatedId);
}

function getSpaceId(app, userId) {
  try {
    var records = app.dao().findRecordsByFilter(
      "couple_spaces",
      "(user_a_id='" + userId + "' || user_b_id='" + userId + "') && status='active'",
      "", 1, 0
    );
    if (records.length > 0) return records[0].getId();
  } catch (e) { }
  return null;
}

function getPartnerId(app, userId) {
  try {
    var records = app.dao().findRecordsByFilter(
      "couple_spaces",
      "(user_a_id='" + userId + "' || user_b_id='" + userId + "') && status='active'",
      "", 1, 0
    );
    if (records.length > 0) {
      var rec = records[0];
      var a = rec.getString("user_a_id");
      var b = rec.getString("user_b_id");
      return (a === userId) ? b : a;
    }
  } catch (e) { }
  return null;
}

// ---- 报备奖励 ----
onRecordAfterCreateRequest("checkins", function(e) {
  var record = e.record;
  var userId = record.getString("user_id");
  var spaceId = record.getString("space_id");
  if (spaceId && userId) {
    rewardCoins(e.app, spaceId, userId, "报备", REWARDS.checkin, DAILY_LIMITS.checkin, record.getId());
  }
});

// ---- 互动奖励/消费 ----
onRecordAfterCreateRequest("interactions", function(e) {
  var record = e.record;
  var fromUser = record.getString("from_user_id");
  var toUser = record.getString("to_user_id");
  var spaceId = record.getString("space_id");
  var type = record.getString("type");
  if (!spaceId || !fromUser || !toUser) return;

  if (type === "hug") {
    spendCoins(e.app, spaceId, fromUser, "送出抱抱", REWARDS.hug_cost, record.getId());
    rewardCoins(e.app, spaceId, toUser, "收到抱抱", REWARDS.hug_recv, 0, record.getId());
  } else if (type === "kiss") {
    spendCoins(e.app, spaceId, fromUser, "送出亲亲", REWARDS.kiss_cost, record.getId());
    rewardCoins(e.app, spaceId, toUser, "收到亲亲", REWARDS.kiss_recv, 0, record.getId());
  } else if (type === "miss") {
    rewardCoins(e.app, spaceId, fromUser, "想你", REWARDS.miss, 10, record.getId());
  }
});

// ---- 日记奖励 ----
onRecordAfterCreateRequest("diaries", function(e) {
  var record = e.record;
  var userId = record.getString("user_id");
  var spaceId = record.getString("space_id");
  if (spaceId && userId) {
    rewardCoins(e.app, spaceId, userId, "写日记", REWARDS.diary, DAILY_LIMITS.diary, record.getId());
  }
});

// ---- 记账奖励 ----
onRecordAfterCreateRequest("expenses", function(e) {
  var record = e.record;
  var paidBy = record.getString("paid_by");
  var spaceId = record.getString("space_id");
  if (spaceId && paidBy) {
    rewardCoins(e.app, spaceId, paidBy, "记账", REWARDS.expense, DAILY_LIMITS.expense, record.getId());
  }
});

// ---- 点赞奖励 ----
onRecordAfterCreateRequest("diary_likes", function(e) {
  var record = e.record;
  var userId = record.getString("user_id");
  if (userId) {
    var spaceId = getSpaceId(e.app, userId);
    if (spaceId) {
      rewardCoins(e.app, spaceId, userId, "点赞", REWARDS.like, DAILY_LIMITS.like, record.getId());
    }
  }
});

// ---- 评论奖励 ----
onRecordAfterCreateRequest("diary_comments", function(e) {
  var record = e.record;
  var userId = record.getString("user_id");
  if (userId) {
    var spaceId = getSpaceId(e.app, userId);
    if (spaceId) {
      rewardCoins(e.app, spaceId, userId, "评论", REWARDS.comment, DAILY_LIMITS.comment, record.getId());
    }
  }
});

// ---- 登录签到奖励 ----
onRecordAfterAuthWithPasswordRequest("users", function(e) {
  var user = e.record;
  if (!user) return;
  var userId = user.getId();
  var spaceId = getSpaceId(e.app, userId);
  if (!spaceId) return;
  rewardCoins(e.app, spaceId, userId, "每日签到", REWARDS.login, 1, "");
});
