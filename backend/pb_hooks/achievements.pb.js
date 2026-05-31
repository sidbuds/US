// ============================================================
// 秘密基地 — 成就 + 连续互动奖励 Hook (PocketBase v0.22)
// ============================================================

// ---- 愿望贡献后检查是否完成 ----
onRecordAfterCreateRequest("wishlist_contributions", function(e) {
  var record = e.record;
  var wishId = record.getString("wish_id");
  var userId = record.getString("user_id");
  var amount = record.getInt("amount") || 0;
  if (!wishId || !userId || amount <= 0) return;

  try {
    var wish = e.app.dao().findRecordById("wishlist", wishId);
    var spaceId = wish.getString("space_id");
    var target = wish.getFloat("target_coins") || 0;
    var current = wish.getFloat("current_balance") || 0;
    var newBalance = current + amount;

    wish.set("current_balance", newBalance);

    if (newBalance >= target && wish.getString("status") !== "completed") {
      wish.set("status", "completed");
      wish.set("completed_at", new Date().toISOString());

      if (spaceId) {
        try {
          var coinRecords = e.app.dao().findRecordsByFilter(
            "coins", "space_id='" + spaceId + "'", "", 1, 0
          );
          if (coinRecords.length > 0) {
            var coin = coinRecords[0];
            var bal = coin.getFloat("balance") || 0;
            var earned = coin.getFloat("total_earned") || 0;
            coin.set("balance", bal + 30);
            coin.set("total_earned", earned + 30);
            e.app.dao().saveRecord(coin);
          }
          var txCollection = e.app.dao().findCollectionByNameOrId("coin_transactions");
          var tx = new Record(txCollection);
          tx.set("space_id", spaceId);
          tx.set("user_id", userId);
          tx.set("type", "earn");
          tx.set("reason", "愿望达成");
          tx.set("amount", 30);
          tx.set("related_id", wishId);
          e.app.dao().saveRecord(tx);
        } catch (ex) { }
      }
    }

    // 扣除贡献者金币
    if (spaceId) {
      try {
        var coinRecords2 = e.app.dao().findRecordsByFilter(
          "coins", "space_id='" + spaceId + "'", "", 1, 0
        );
        if (coinRecords2.length > 0) {
          var coin2 = coinRecords2[0];
          var bal2 = coin2.getFloat("balance") || 0;
          var spent2 = coin2.getFloat("total_spent") || 0;
          coin2.set("balance", bal2 - amount);
          coin2.set("total_spent", spent2 + amount);
          e.app.dao().saveRecord(coin2);
        }
        var txCollection2 = e.app.dao().findCollectionByNameOrId("coin_transactions");
        var tx2 = new Record(txCollection2);
        tx2.set("space_id", spaceId);
        tx2.set("user_id", userId);
        tx2.set("type", "spend");
        tx2.set("reason", "愿望贡献");
        tx2.set("amount", amount);
        tx2.set("related_id", wishId);
        e.app.dao().saveRecord(tx2);
      } catch (ex) { }
    }

    e.app.dao().saveRecord(wish);
  } catch (err) { }
});

// ---- 连续互动奖励 ----
onRecordAfterCreateRequest("interactions", function(e) {
  var record = e.record;
  var spaceId = record.getString("space_id");
  var fromUser = record.getString("from_user_id");
  if (!spaceId || !fromUser) return;

  try {
    var allDaysOk = true;
    for (var i = 0; i < 3; i++) {
      var d = new Date();
      d.setDate(d.getDate() - i);
      var dayStr = d.getFullYear() + "-" +
        ("0" + (d.getMonth() + 1)).slice(-2) + "-" +
        ("0" + d.getDate()).slice(-2);
      var nextDay = new Date(d);
      nextDay.setDate(nextDay.getDate() + 1);
      var nextStr = nextDay.getFullYear() + "-" +
        ("0" + (nextDay.getMonth() + 1)).slice(-2) + "-" +
        ("0" + nextDay.getDate()).slice(-2);

      var dayRecords = e.app.dao().findRecordsByFilter(
        "interactions",
        "space_id='" + spaceId + "' && created>='" + dayStr + "' && created<'" + nextStr + "'",
        "-created", 1, 0
      );
      if (dayRecords.length === 0) {
        allDaysOk = false;
        break;
      }
    }

    if (allDaysOk) {
      var today = new Date();
      var todayStr = today.getFullYear() + "-" +
        ("0" + (today.getMonth() + 1)).slice(-2) + "-" +
        ("0" + today.getDate()).slice(-2);

      var bonusTx = e.app.dao().findRecordsByFilter(
        "coin_transactions",
        "space_id='" + spaceId + "' && reason='连续互动奖励' && created>='" + todayStr + "'",
        "", 1, 0
      );

      if (bonusTx.length === 0) {
        var coinRecords = e.app.dao().findRecordsByFilter(
          "coins", "space_id='" + spaceId + "'", "", 1, 0
        );
        if (coinRecords.length > 0) {
          var coin = coinRecords[0];
          var bal = coin.getFloat("balance") || 0;
          var earned = coin.getFloat("total_earned") || 0;
          coin.set("balance", bal + 50);
          coin.set("total_earned", earned + 50);
          e.app.dao().saveRecord(coin);

          var txCollection = e.app.dao().findCollectionByNameOrId("coin_transactions");
          var tx = new Record(txCollection);
          tx.set("space_id", spaceId);
          tx.set("user_id", fromUser);
          tx.set("type", "earn");
          tx.set("reason", "连续互动奖励");
          tx.set("amount", 50);
          tx.set("related_id", "");
          e.app.dao().saveRecord(tx);
        }
      }
    }
  } catch (err) { }
});
