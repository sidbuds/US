// ============================================================
// 秘密基地 — 数据校验 Hook (PocketBase v0.22)
// ============================================================

onRecordBeforeCreateRequest("interactions", function(e) {
  var record = e.record;
  var fromUser = record.getString("from_user_id");
  var spaceId = record.getString("space_id");
  var type = record.getString("type");
  if (!fromUser || !spaceId) return;

  var today = new Date();
  var todayStr = today.getFullYear() + "-" +
    ("0" + (today.getMonth() + 1)).slice(-2) + "-" +
    ("0" + today.getDate()).slice(-2);

  var limit = (type === "hug") ? 10 : (type === "kiss") ? 10 : 20;

  try {
    var records = e.app.dao().findRecordsByFilter(
      "interactions",
      "space_id='" + spaceId + "' && from_user_id='" + fromUser + "' && type='" + type + "' && created>='" + todayStr + "'",
      "-created", 100, 0
    );
    if (records.length >= limit) {
      throw new BadRequestError("今日" + type + "次数已达上限 (" + limit + "次)");
    }
  } catch (err) {
    if (err.message && err.message.indexOf("已达上限") >= 0) throw err;
  }
});

onRecordBeforeCreateRequest("checkins", function(e) {
  var record = e.record;
  var userId = record.getString("user_id");
  var spaceId = record.getString("space_id");
  if (!userId || !spaceId) return;

  var today = new Date();
  var todayStr = today.getFullYear() + "-" +
    ("0" + (today.getMonth() + 1)).slice(-2) + "-" +
    ("0" + today.getDate()).slice(-2);

  try {
    var records = e.app.dao().findRecordsByFilter(
      "checkins",
      "space_id='" + spaceId + "' && user_id='" + userId + "' && created>='" + todayStr + "'",
      "-created", 100, 0
    );
    if (records.length >= 7) {
      throw new BadRequestError("今日报备次数已达上限 (7次)");
    }
  } catch (err) {
    if (err.message && err.message.indexOf("已达上限") >= 0) throw err;
  }
});

onRecordBeforeCreateRequest("diaries", function(e) {
  var record = e.record;
  var userId = record.getString("user_id");
  var spaceId = record.getString("space_id");
  if (!userId || !spaceId) return;

  var today = new Date();
  var todayStr = today.getFullYear() + "-" +
    ("0" + (today.getMonth() + 1)).slice(-2) + "-" +
    ("0" + today.getDate()).slice(-2);

  try {
    var records = e.app.dao().findRecordsByFilter(
      "diaries",
      "space_id='" + spaceId + "' && user_id='" + userId + "' && created>='" + todayStr + "'",
      "-created", 100, 0
    );
    if (records.length >= 2) {
      throw new BadRequestError("今日写日记已达上限 (2篇)");
    }
  } catch (err) {
    if (err.message && err.message.indexOf("已达上限") >= 0) throw err;
  }
});

onRecordBeforeCreateRequest("wishlist_contributions", function(e) {
  var record = e.record;
  var wishId = record.getString("wish_id");
  var userId = record.getString("user_id");
  var amount = record.getInt("amount") || 0;
  if (!wishId || !userId || amount <= 0) return;

  try {
    var wish = e.app.dao().findRecordById("wishlist", wishId);
    var spaceId = wish.getString("space_id");
    var coinRecords = e.app.dao().findRecordsByFilter(
      "coins", "space_id='" + spaceId + "'", "", 1, 0
    );
    if (coinRecords.length > 0) {
      var balance = coinRecords[0].getFloat("balance") || 0;
      if (balance < amount) {
        throw new BadRequestError("金币余额不足 (当前: " + balance + ", 需要: " + amount + ")");
      }
    }
  } catch (err) {
    if (err.message && err.message.indexOf("不足") >= 0) throw err;
  }
});
