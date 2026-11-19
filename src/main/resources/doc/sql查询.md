查询出GROUPID最大的那一行的数据，要求RGST_DT和GROUP_ID在同一行，请修改以下SQL语句：
```
SELECT
  "PCKG_NM",
  MAX("GROUP_ID") AS "GROUP_ID",
  MAX("RGST_DT") AS "RGST_DT"
FROM
  "FUMO_PACKAGE_TEST"
WHERE
  "PCKG_ST" != '50'
  AND "PCKG_ST" != '80'
  AND "PCKG_ST" != '110'
  AND "PCKG_ST" != '120'
  AND "PCKG_ST" != '180'
  AND "PCKG_ST" != '190'
  AND "PCKG_ST" != '200'
GROUP BY
  "PCKG_NM"
```



你现在的写法分别用了 MAX("GROUP_ID") 和 MAX("RGST_DT")，这两个最大值可能来自不同的行。如果要求：

每个 PCKG_NM 找到 GROUP_ID 最大的那一行，并返回该行对应的 RGST_DT

建议使用窗口函数 ROW_NUMBER()。

推荐写法
SELECT
"PCKG_NM",
"GROUP_ID",
"RGST_DT"
FROM (
SELECT
"PCKG_NM",
"GROUP_ID",
"RGST_DT",
ROW_NUMBER() OVER (
PARTITION BY "PCKG_NM"
ORDER BY "GROUP_ID" DESC
) AS rn
FROM "FUMO_PACKAGE_TEST"
WHERE "PCKG_ST" NOT IN ('50', '80', '110', '120', '180', '190', '200')
) t
WHERE rn = 1;

这样可以保证

例如原始数据：

PCKG_NM	GROUP_ID	RGST_DTA	100	2026-10-01
A	200	2026-09-01
A	150	2026-11-01

你的原 SQL：

MAX("GROUP_ID")
MAX("RGST_DT")


会得到：

A | 200 | 2026-11-01


但数据库中实际上不存在这一行。

修改后的 SQL 会得到：

A | 200 | 2026-09-01


这样 "GROUP_ID" 和 "RGST_DT" 就一定来自同一条记录。

如果 GROUP_ID 可能重复

如果同一个 "PCKG_NM" 下可能出现多个相同的最大 "GROUP_ID"，建议再增加一个排序条件，例如取其中 RGST_DT 最新的一条：

ROW_NUMBER() OVER (
PARTITION BY "PCKG_NM"
ORDER BY "GROUP_ID" DESC, "RGST_DT" DESC
) AS rn


完整 SQL：

SELECT
"PCKG_NM",
"GROUP_ID",
"RGST_DT"
FROM (
SELECT
"PCKG_NM",
"GROUP_ID",
"RGST_DT",
ROW_NUMBER() OVER (
PARTITION BY "PCKG_NM"
ORDER BY "GROUP_ID" DESC, "RGST_DT" DESC
) AS rn
FROM "FUMO_PACKAGE_TEST"
WHERE "PCKG_ST" NOT IN ('50', '80', '110', '120', '180', '190', '200')
) t
WHERE rn = 1;


主要修改点： 不再分别计算 MAX(GROUP_ID) 和 MAX(RGST_DT)，而是先按照 PCKG_NM 分组排序，再直接取 GROUP_ID 最大的整行数据。如果你这里用的是 PostgreSQL，还可以用 DISTINCT ON 写得更简洁。





2。
以下SQL语句能否简化
```
SELECT
  B."PCKG_NM",
  B."SN",
  B."GROUP_ID",
  B."RGST_DT"
FROM
  "FUMO_PACKAGE_TEST" B
  INNER JOIN (
    SELECT
      "PCKG_NM",
      "GROUP_ID",
      "RGST_DT"
    FROM
      (
        SELECT
          "PCKG_NM",
          "GROUP_ID",
          "RGST_DT",
          ROW_NUMBER() OVER (PARTITION BY "PCKG_NM" ORDER BY "RGST_DT" DESC) AS rn
        FROM
          "FUMO_PACKAGE_TEST"
        WHERE
          "PCKG_ST" NOT IN ('50', '80', '110', '120', '180', '190', '200')
      ) t
    WHERE
      rn = 1
  ) A ON B."PCKG_NM" = A."PCKG_NM"
  AND B."GROUP_ID" = A."GROUP_ID"
  AND B."RGST_DT" = A."RGST_DT"
  ```

可以，而且你这个 SQL 可以明显简化。

你现在外层再 INNER JOIN 一次 "FUMO_PACKAGE_TEST"，主要是为了取得 "SN"。实际上可以直接在 ROW_NUMBER() 的查询中把 "SN" 一起取出来，这样就不需要 JOIN。

推荐简化为
SELECT
"PCKG_NM",
"SN",
"GROUP_ID",
"RGST_DT"
FROM (
SELECT
"PCKG_NM",
"SN",
"GROUP_ID",
"RGST_DT",
ROW_NUMBER() OVER (
PARTITION BY "PCKG_NM"
ORDER BY "RGST_DT" DESC
) AS rn
FROM "FUMO_PACKAGE_TEST"
WHERE "PCKG_ST" NOT IN ('50', '80', '110', '120', '180', '190', '200')
) t
WHERE rn = 1;


这样逻辑就是：

每个 PCKG_NM 中，过滤掉指定状态后，取得 RGST_DT 最大的那一整行。

因此：

PCKG_NM
SN
GROUP_ID
RGST_DT

四个字段一定来自同一行。

如果你实际要求是 GROUP_ID 最大

需要注意，你现在的 SQL 排序条件是：

ORDER BY "RGST_DT" DESC


所以现在取得的是：

RGST_DT 最新的一行

如果你真正想实现上一问中的需求：

每个 PCKG_NM 查询 GROUP_ID 最大的那一行，同时取得这一行的 SN、RGST_DT

应该改成：

SELECT
"PCKG_NM",
"SN",
"GROUP_ID",
"RGST_DT"
FROM (
SELECT
"PCKG_NM",
"SN",
"GROUP_ID",
"RGST_DT",
ROW_NUMBER() OVER (
PARTITION BY "PCKG_NM"
ORDER BY "GROUP_ID" DESC
) AS rn
FROM "FUMO_PACKAGE_TEST"
WHERE "PCKG_ST" NOT IN ('50', '80', '110', '120', '180', '190', '200')
) t
WHERE rn = 1;

更严谨一点

如果相同 PCKG_NM 可能存在相同的 GROUP_ID，建议指定第二排序条件，否则数据库可以任意选择其中一行。

例如相同 GROUP_ID 时取 RGST_DT 最新的：

SELECT
"PCKG_NM",
"SN",
"GROUP_ID",
"RGST_DT"
FROM (
SELECT
"PCKG_NM",
"SN",
"GROUP_ID",
"RGST_DT",
ROW_NUMBER() OVER (
PARTITION BY "PCKG_NM"
ORDER BY
"GROUP_ID" DESC,
"RGST_DT" DESC
) AS rn
FROM "FUMO_PACKAGE_TEST"
WHERE "PCKG_ST" NOT IN ('50', '80', '110', '120', '180', '190', '200')
) t
WHERE rn = 1;


我更推荐最后这个版本。它不仅比原 SQL 少一次表连接，而且明确保证 SN/GROUP_ID/RGST_DT 来自同一条记录，并处理了 GROUP_ID 重复时的情况。


3. 以下SQL语句能否简化
```
select B."PCKG_NM", CAST(MAX(CAST(B."SN" AS INTEGER)) AS VARCHAR) as "SN", B."GROUP_ID", B."RGST_DT"
FROM
  "FUMO_PACKAGE_TEST" B
  INNER JOIN (
    SELECT
      "PCKG_NM",
      "GROUP_ID",
      "RGST_DT"
    FROM
      (
        SELECT
          "PCKG_NM",
          "GROUP_ID",
          "RGST_DT",
          ROW_NUMBER() OVER (PARTITION BY "PCKG_NM" ORDER BY "RGST_DT" DESC) AS rn
        FROM
          "FUMO_PACKAGE_TEST"
        WHERE
          "PCKG_ST" NOT IN ('50', '80', '110', '120', '180', '190', '200')
      ) t
    WHERE
      rn = 1
  ) A ON B."PCKG_NM" = A."PCKG_NM"
  AND B."GROUP_ID" = A."GROUP_ID"
  AND B."RGST_DT" = A."RGST_DT"
  group by B."PCKG_NM", B."GROUP_ID", B."RGST_DT"
  ```

可以简化，而且你这一版和上一版有个关键区别：

先找到每个 PCKG_NM 中 RGST_DT 最新的记录组，然后如果该 PCKG_NM + GROUP_ID + RGST_DT 存在多条记录，再取其中数值最大的 SN。

所以不能简单地把 SN 放进之前的 ROW_NUMBER() 就结束，除非把 SN 作为第二排序条件。

推荐简化写法

如果 "SN" 是 VARCHAR，但内容是数字，可以直接：

SELECT
"PCKG_NM",
"SN",
"GROUP_ID",
"RGST_DT"
FROM (
SELECT
"PCKG_NM",
"SN",
"GROUP_ID",
"RGST_DT",
ROW_NUMBER() OVER (
PARTITION BY "PCKG_NM"
ORDER BY
"RGST_DT" DESC,
CAST("SN" AS INTEGER) DESC
) AS rn
FROM "FUMO_PACKAGE_TEST"
WHERE "PCKG_ST" NOT IN ('50', '80', '110', '120', '180', '190', '200')
) T
WHERE rn = 1;


这样就不需要：

INNER JOIN
GROUP BY
MAX(SN)
外层再次查询 "FUMO_PACKAGE_TEST"
但要注意一个逻辑差异

你的原 SQL 中，PCKG_ST 条件只参与确定最新的 RGST_DT。

找到 PCKG_NM + GROUP_ID + RGST_DT 后，再 JOIN 回 B 表时：

FROM "FUMO_PACKAGE_TEST" B


这里的 B 没有 PCKG_ST 限制。

所以，如果同一个：

PCKG_NM + GROUP_ID + RGST_DT


存在多行，而且其中一些行的 PCKG_ST='50'，你的原 SQL 的：

MAX(CAST(B."SN" AS INTEGER))


仍然会把 PCKG_ST='50' 的行算进去。

而我上面的简化版本不会。

如果你的真实需求是过滤 PCKG_ST 后再选最大 SN

那么我建议就使用这个版本：

SELECT
"PCKG_NM",
"SN",
"GROUP_ID",
"RGST_DT"
FROM (
SELECT
"PCKG_NM",
"SN",
"GROUP_ID",
"RGST_DT",
ROW_NUMBER() OVER (
PARTITION BY "PCKG_NM"
ORDER BY
"RGST_DT" DESC,
CAST("SN" AS INTEGER) DESC
) AS rn
FROM "FUMO_PACKAGE_TEST"
WHERE "PCKG_ST" NOT IN ('50', '80', '110', '120', '180', '190', '200')
) T
WHERE rn = 1;


比如：

PCKG_NM   SN    GROUP_ID   RGST_DT
--------  ----  ---------  -------------------
AAA       1     100         2026-11-01 10:00
AAA       2     100         2026-11-02 10:00
AAA       3     100         2026-11-02 10:00


排序实际上就是：

RGST_DT DESC
↓
SN DESC


最终得到：

AAA   3   100   2026-11-02 10:00


也就是：

先取最新 RGST_DT，如果最新时间存在多行，再取最大的数字 SN。

这个写法比你原来的 JOIN + GROUP BY + MAX() 更直观，通常也是我更推荐的写法。

另外，如果 "SN" 可能存在空字符串或非数字，例如 ''、ABC，CAST("SN" AS INTEGER) 在 PostgreSQL 中会报错，这种情况需要再做一下安全转换。


4. 以下SQL语句能否简化
```
select F."PCKG_NM", E."SRC_VER", E."TGT_VER", E."SN", E."PCKG_ST", fn_common_cd_nm('packageStatus', E."PCKG_ST", 'zh') as
        "PCKG_ST_NM", E."VER_ALIAS", E."ISSUE_TYPE", count(*) over() as "TOTAL"
        from "PACKAGE_INFO" F
        left outer join (
    SELECT
      C."PCKG_NM",
      C."SRC_VER",
      C."TGT_VER",
      C."SN",
      C."PCKG_ST",
      C."VER_ALIAS",
      C."ISSUE_TYPE"
    FROM
      "FUMO_PACKAGE_TEST" C
      INNER JOIN (
SELECT
    "PCKG_NM",
    "SN",
    "GROUP_ID",
    "RGST_DT"
FROM (
    SELECT
        "PCKG_NM",
        "SN",
        "GROUP_ID",
        "RGST_DT",
        ROW_NUMBER() OVER (
            PARTITION BY "PCKG_NM"
            ORDER BY
                "RGST_DT" DESC,
                CAST("SN" AS INTEGER) DESC
        ) AS rn
    FROM "FUMO_PACKAGE_TEST"
    WHERE "PCKG_ST" NOT IN ('50', '80', '110', '120', '180', '190', '200')
) T
WHERE rn = 1
            ) D
            on C."PCKG_NM" = D."PCKG_NM"
            and C."SN" = D."SN"
            and C."GROUP_ID" = D."GROUP_ID"
            and C."RGST_DT" = D."RGST_DT"
        ) E
        on F."PCKG_NM" = E."PCKG_NM"
        WHERE 1=1
        AND "USE_YN" = 'Y'
        ORDER BY "RGST_DT" DESC
        LIMIT 10 OFFSET 20
  ```

可以，而且这条 SQL 可以少一层 INNER JOIN。

你现在 D 子查询已经通过 ROW_NUMBER() 找到了每个 PCKG_NM 最新 RGST_DT、且同一时间下 SN 最大的那一行，但随后又 JOIN 回 FUMO_PACKAGE_TEST C 只是为了取 SRC_VER / TGT_VER / PCKG_ST / VER_ALIAS / ISSUE_TYPE。这些字段完全可以在窗口函数查询时一起带出来。

推荐简化版
SELECT
F."PCKG_NM",
E."SRC_VER",
E."TGT_VER",
E."SN",
E."PCKG_ST",
fn_common_cd_nm('packageStatus', E."PCKG_ST", 'zh') AS "PCKG_ST_NM",
E."VER_ALIAS",
E."ISSUE_TYPE",
COUNT(*) OVER() AS "TOTAL"
FROM "PACKAGE_INFO" F
LEFT JOIN (
SELECT
T."PCKG_NM",
T."SRC_VER",
T."TGT_VER",
T."SN",
T."PCKG_ST",
T."VER_ALIAS",
T."ISSUE_TYPE",
T."RGST_DT"
FROM (
SELECT
"PCKG_NM",
"SRC_VER",
"TGT_VER",
"SN",
"PCKG_ST",
"VER_ALIAS",
"ISSUE_TYPE",
"RGST_DT",
ROW_NUMBER() OVER (
PARTITION BY "PCKG_NM"
ORDER BY
"RGST_DT" DESC,
CAST("SN" AS INTEGER) DESC
) AS rn
FROM "FUMO_PACKAGE_TEST"
WHERE "PCKG_ST" NOT IN (
'50', '80', '110', '120',
'180', '190', '200'
)
) T
WHERE T.rn = 1
) E
ON F."PCKG_NM" = E."PCKG_NM"
WHERE F."USE_YN" = 'Y'
ORDER BY E."RGST_DT" DESC
LIMIT 10
OFFSET 20;

逻辑也更清楚

现在执行过程直接变成：

FUMO_PACKAGE_TEST
↓
排除指定 PCKG_ST
↓
按 PCKG_NM 分组
↓
RGST_DT DESC
↓
相同 RGST_DT 时 SN DESC
↓
ROW_NUMBER() = 1
↓
每个 PCKG_NM 得到一条记录
↓
LEFT JOIN PACKAGE_INFO
↓
USE_YN = 'Y'
↓
分页


也就是每个 PCKG_NM：

排除状态：

50, 80, 110, 120, 180, 190, 200


找最大的：

"RGST_DT"


如果最大 RGST_DT 有多条，再找：

CAST("SN" AS INTEGER)


最大的一条。

这一行的：

SRC_VER
TGT_VER
SN
PCKG_ST
VER_ALIAS
ISSUE_TYPE
RGST_DT


全部来自同一行。

PostgreSQL 还可以继续简化

从你之前的环境来看，如果这里使用的是 PostgreSQL，我反而更推荐 DISTINCT ON。这个需求正好非常适合 PostgreSQL 的这个特性：

SELECT
F."PCKG_NM",
E."SRC_VER",
E."TGT_VER",
E."SN",
E."PCKG_ST",
fn_common_cd_nm('packageStatus', E."PCKG_ST", 'zh') AS "PCKG_ST_NM",
E."VER_ALIAS",
E."ISSUE_TYPE",
COUNT(*) OVER() AS "TOTAL"
FROM "PACKAGE_INFO" F
LEFT JOIN (
SELECT DISTINCT ON ("PCKG_NM")
"PCKG_NM",
"SRC_VER",
"TGT_VER",
"SN",
"PCKG_ST",
"VER_ALIAS",
"ISSUE_TYPE",
"RGST_DT"
FROM "FUMO_PACKAGE_TEST"
WHERE "PCKG_ST" NOT IN (
'50', '80', '110', '120',
'180', '190', '200'
)
ORDER BY
"PCKG_NM",
"RGST_DT" DESC,
CAST("SN" AS INTEGER) DESC
) E
ON F."PCKG_NM" = E."PCKG_NM"
WHERE F."USE_YN" = 'Y'
ORDER BY E."RGST_DT" DESC
LIMIT 10 OFFSET 20;


我更推荐这个版本，原因是它非常直接地表达：

每个 PCKG_NM 只取第一条，而第一条按照 RGST_DT DESC, SN DESC 确定。

性能方面再提醒一个点

当前这里：

CAST("SN" AS INTEGER)


会影响普通 "SN" 索引的利用。如果 SN 本身永远是数字，长期来看最好把字段设计成 INTEGER/BIGINT。

如果暂时不能改字段类型，而数据量比较大，可以考虑 PostgreSQL 表达式索引，例如：

CREATE INDEX idx_fumo_package_test_latest
ON "FUMO_PACKAGE_TEST" (
"PCKG_NM",
"RGST_DT" DESC,
(CAST("SN" AS INTEGER)) DESC
);


不过不要直接创建这个索引，因为还要结合 "FUMO_PACKAGE_TEST" 的数据量、现有索引以及 PCKG_ST 的数据分布来决定。对于你这条 SQL，PCKG_NM + RGST_DT + SN 的索引设计是下一步最值得优化的地方。