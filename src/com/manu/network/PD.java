package com.manu.network;

import qxmobile.protobuf.AchievementProtos.AcheFinishInform;
import qxmobile.protobuf.AchievementProtos.AcheGetRewardRequest;
import qxmobile.protobuf.AchievementProtos.AcheGetRewardResponse;
import qxmobile.protobuf.AchievementProtos.AcheListResponse;
import qxmobile.protobuf.AllianceFightProtos.ApplyFightResp;
import qxmobile.protobuf.AllianceFightProtos.BattlefieldInfoNotify;
import qxmobile.protobuf.AllianceFightProtos.BattlefieldInfoResp;
import qxmobile.protobuf.AllianceFightProtos.BufferInfo;
import qxmobile.protobuf.AllianceFightProtos.FightAttackReq;
import qxmobile.protobuf.AllianceFightProtos.FightAttackResp;
import qxmobile.protobuf.AllianceFightProtos.FightHistoryResp;
import qxmobile.protobuf.AllianceFightProtos.FightLasttimeRankResp;
import qxmobile.protobuf.AllianceFightProtos.PlayerDeadNotify;
import qxmobile.protobuf.AllianceFightProtos.PlayerReviveNotify;
import qxmobile.protobuf.AllianceFightProtos.PlayerReviveRequest;
import qxmobile.protobuf.AllianceFightProtos.RequestFightInfoResp;
import qxmobile.protobuf.AllianceProtos.AgreeApply;
import qxmobile.protobuf.AllianceProtos.AgreeApplyResp;
import qxmobile.protobuf.AllianceProtos.AllianceHaveResp;
import qxmobile.protobuf.AllianceProtos.AllianceNonResp;
import qxmobile.protobuf.AllianceProtos.ApplyAlliance;
import qxmobile.protobuf.AllianceProtos.ApplyAllianceResp;
import qxmobile.protobuf.AllianceProtos.CancelJoinAlliance;
import qxmobile.protobuf.AllianceProtos.CancelJoinAllianceResp;
import qxmobile.protobuf.AllianceProtos.CheckAllianceName;
import qxmobile.protobuf.AllianceProtos.CheckAllianceNameResp;
import qxmobile.protobuf.AllianceProtos.CloseApply;
import qxmobile.protobuf.AllianceProtos.CreateAlliance;
import qxmobile.protobuf.AllianceProtos.CreateAllianceResp;
import qxmobile.protobuf.AllianceProtos.DismissAlliance;
import qxmobile.protobuf.AllianceProtos.DonateHuFu;
import qxmobile.protobuf.AllianceProtos.DonateHuFuResp;
import qxmobile.protobuf.AllianceProtos.DownTitle;
import qxmobile.protobuf.AllianceProtos.DownTitleResp;
import qxmobile.protobuf.AllianceProtos.EventListResp;
import qxmobile.protobuf.AllianceProtos.ExitAlliance;
import qxmobile.protobuf.AllianceProtos.ExitAllianceResp;
import qxmobile.protobuf.AllianceProtos.FindAlliance;
import qxmobile.protobuf.AllianceProtos.FindAllianceResp;
import qxmobile.protobuf.AllianceProtos.FireMember;
import qxmobile.protobuf.AllianceProtos.FireMemberResp;
import qxmobile.protobuf.AllianceProtos.GiveUpVoteResp;
import qxmobile.protobuf.AllianceProtos.LookApplicants;
import qxmobile.protobuf.AllianceProtos.LookApplicantsResp;
import qxmobile.protobuf.AllianceProtos.LookMembers;
import qxmobile.protobuf.AllianceProtos.LookMembersResp;
import qxmobile.protobuf.AllianceProtos.MengZhuApplyResp;
import qxmobile.protobuf.AllianceProtos.MengZhuVote;
import qxmobile.protobuf.AllianceProtos.MengZhuVoteResp;
import qxmobile.protobuf.AllianceProtos.OpenApply;
import qxmobile.protobuf.AllianceProtos.OpenApplyResp;
import qxmobile.protobuf.AllianceProtos.RefuseApply;
import qxmobile.protobuf.AllianceProtos.RefuseApplyResp;
import qxmobile.protobuf.AllianceProtos.TransferAlliance;
import qxmobile.protobuf.AllianceProtos.TransferAllianceResp;
import qxmobile.protobuf.AllianceProtos.UpTitle;
import qxmobile.protobuf.AllianceProtos.UpTitleResp;
import qxmobile.protobuf.AllianceProtos.UpdateNotice;
import qxmobile.protobuf.AllianceProtos.UpdateNoticeResp;
import qxmobile.protobuf.AllianceProtos.immediatelyJoin;
import qxmobile.protobuf.AllianceProtos.immediatelyJoinResp;
import qxmobile.protobuf.BagOperProtos.BagInfo;
import qxmobile.protobuf.BagOperProtos.EquipAddReq;
import qxmobile.protobuf.BagOperProtos.EquipDetail;
import qxmobile.protobuf.BagOperProtos.EquipDetailReq;
import qxmobile.protobuf.BagOperProtos.EquipInfo;
import qxmobile.protobuf.BagOperProtos.EquipInfoOtherReq;
import qxmobile.protobuf.BagOperProtos.EquipRemoveReq;
import qxmobile.protobuf.BagOperProtos.YuJueHeChengResult;
import qxmobile.protobuf.BattleProg.InProgress;
import qxmobile.protobuf.BattleProg.InitProc;
import qxmobile.protobuf.BattlePveInit.BattleInit;
import qxmobile.protobuf.BattlePveInit.BattlePveInitReq;
import qxmobile.protobuf.BattlePveInit.BattlePvpInitReq;
import qxmobile.protobuf.BattlePveInit.BattleReplayData;
import qxmobile.protobuf.BattlePveInit.BattleReplayReq;
import qxmobile.protobuf.BattlePveInit.Hero;
import qxmobile.protobuf.BattlePveInit.Soldier;
import qxmobile.protobuf.BattlePveInit.Troop;
import qxmobile.protobuf.BattlePveResult.AwardItem;
import qxmobile.protobuf.BattlePveResult.BattleResult;
import qxmobile.protobuf.BattlePveResult.BattleResultAllianceFight;
import qxmobile.protobuf.CDKey.GetCDKeyAwardReq;
import qxmobile.protobuf.CDKey.GetCDKeyAwardResp;
import qxmobile.protobuf.Cards.BuyCardBagReq;
import qxmobile.protobuf.Cards.BuyCardBagResp;
import qxmobile.protobuf.Cards.OpenCardBagReq;
import qxmobile.protobuf.Cards.OpenCardBagResp;
import qxmobile.protobuf.Chat.CGetChat;
import qxmobile.protobuf.Chat.CGetYuYing;
import qxmobile.protobuf.Chat.CancelBlack;
import qxmobile.protobuf.Chat.ChatPct;
import qxmobile.protobuf.Chat.JoinToBlacklist;
import qxmobile.protobuf.Chat.SChatLogList;
import qxmobile.protobuf.DailyAwardProto.GetDailyAward;
import qxmobile.protobuf.DailyTaskProtos.DailyTaskFinishInform;
import qxmobile.protobuf.DailyTaskProtos.DailyTaskListResponse;
import qxmobile.protobuf.DailyTaskProtos.DailyTaskRewardRequest;
import qxmobile.protobuf.DailyTaskProtos.DailyTaskRewardResponse;
import qxmobile.protobuf.EmailProtos.DeleteEmailResp;
import qxmobile.protobuf.EmailProtos.EmailListResponse;
import qxmobile.protobuf.EmailProtos.EmailResponse;
import qxmobile.protobuf.EmailProtos.EmailResponseResult;
import qxmobile.protobuf.EmailProtos.GetRewardRequest;
import qxmobile.protobuf.EmailProtos.GetRewardResponse;
import qxmobile.protobuf.EmailProtos.NewMailResponse;
import qxmobile.protobuf.EmailProtos.ReadEmail;
import qxmobile.protobuf.EmailProtos.ReadEmailResp;
import qxmobile.protobuf.EmailProtos.SendEmail;
import qxmobile.protobuf.EmailProtos.SendEmailResp;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Explore.ExploreInfoResp;
import qxmobile.protobuf.Explore.ExploreReq;
import qxmobile.protobuf.Explore.ExploreResp;
import qxmobile.protobuf.FriendsProtos.AddFriendReq;
import qxmobile.protobuf.FriendsProtos.GetFriendListReq;
import qxmobile.protobuf.FriendsProtos.RemoveFriendReq;
import qxmobile.protobuf.FuWen.FuwenResp;
import qxmobile.protobuf.FuWen.OperateFuwenReq;
import qxmobile.protobuf.FuWen.QueryFuwenResp;
import qxmobile.protobuf.GameTask.GetTaskReward;
import qxmobile.protobuf.GameTask.GetTaskRwardResult;
import qxmobile.protobuf.GameTask.TaskList;
import qxmobile.protobuf.GameTask.TaskProgress;
import qxmobile.protobuf.GameTask.TaskSync;
import qxmobile.protobuf.GuoJia.GuoJiaMainInfoResp;
import qxmobile.protobuf.GuoJia.JuanXianDayAwardResp;
import qxmobile.protobuf.GuoJia.JuanXianGongJinResp;
import qxmobile.protobuf.House.AnswerExchange;
import qxmobile.protobuf.House.ApplyInfos;
import qxmobile.protobuf.House.BatchSimpleInfo;
import qxmobile.protobuf.House.EnterOrExitHouse;
import qxmobile.protobuf.House.ExCanJuanJiangLi;
import qxmobile.protobuf.House.ExItemResult;
import qxmobile.protobuf.House.ExchangeEHouse;
import qxmobile.protobuf.House.ExchangeHouse;
import qxmobile.protobuf.House.ExchangeItem;
import qxmobile.protobuf.House.ExchangeResult;
import qxmobile.protobuf.House.HouseExpInfo;
import qxmobile.protobuf.House.HouseVisitorInfo;
import qxmobile.protobuf.House.HuanWuInfo;
import qxmobile.protobuf.House.LianMengBoxes;
import qxmobile.protobuf.House.OffVisitorInfo;
import qxmobile.protobuf.House.SetHouseState;
import qxmobile.protobuf.House.setHuanWu;
import qxmobile.protobuf.HuangYeProtos.ActiveTreasureReq;
import qxmobile.protobuf.HuangYeProtos.ActiveTreasureResp;
import qxmobile.protobuf.HuangYeProtos.HYTreasureBattle;
import qxmobile.protobuf.HuangYeProtos.HYTreasureBattleResp;
import qxmobile.protobuf.HuangYeProtos.HyBuyBattleTimesResp;
import qxmobile.protobuf.HuangYeProtos.MaxDamageRankReq;
import qxmobile.protobuf.HuangYeProtos.MaxDamageRankResp;
import qxmobile.protobuf.HuangYeProtos.OpenHuangYeResp;
import qxmobile.protobuf.HuangYeProtos.OpenHuangYeTreasure;
import qxmobile.protobuf.HuangYeProtos.OpenHuangYeTreasureResp;
import qxmobile.protobuf.JiNengPeiYang.GetJiNengPeiYangQuality;
import qxmobile.protobuf.JiNengPeiYang.UpgradeJiNengReq;
import qxmobile.protobuf.JiNengPeiYang.UpgradeJiNengResp;
import qxmobile.protobuf.JingMaiProto.JingMaiReq;
import qxmobile.protobuf.JingMaiProto.JingMaiRet;
import qxmobile.protobuf.JingMaiProto.XueWeiUpReq;
import qxmobile.protobuf.JunZhuProto.BuyTimesInfo;
import qxmobile.protobuf.JunZhuProto.JunZhuInfoSpecifyReq;
import qxmobile.protobuf.JunZhuProto.PveMiBaoZhanLi;
import qxmobile.protobuf.JunZhuProto.TalentInfoResp;
import qxmobile.protobuf.JunZhuProto.TalentUpLevelReq;
import qxmobile.protobuf.JunZhuProto.TalentUpLevelResp;
import qxmobile.protobuf.KeJiProto.KeJiInfoReq;
import qxmobile.protobuf.KeJiProto.KeJiInfoRet;
import qxmobile.protobuf.KeJiProto.KeJiShengJiReq;
import qxmobile.protobuf.LveDuo.LveBattleEndReq;
import qxmobile.protobuf.LveDuo.LveBattleEndResp;
import qxmobile.protobuf.LveDuo.LveBattleRecordResp;
import qxmobile.protobuf.LveDuo.LveConfirmReq;
import qxmobile.protobuf.LveDuo.LveConfirmResp;
import qxmobile.protobuf.LveDuo.LveDuoInfoResp;
import qxmobile.protobuf.LveDuo.LveGoLveDuoReq;
import qxmobile.protobuf.LveDuo.LveGoLveDuoResp;
import qxmobile.protobuf.LveDuo.LveHelpReq;
import qxmobile.protobuf.LveDuo.LveNextItemReq;
import qxmobile.protobuf.LveDuo.LveNextItemResp;
import qxmobile.protobuf.MibaoProtos.MiBaoDealSkillReq;
import qxmobile.protobuf.MibaoProtos.MiBaoDealSkillResp;
import qxmobile.protobuf.MibaoProtos.MibaoActivate;
import qxmobile.protobuf.MibaoProtos.MibaoActivateResp;
import qxmobile.protobuf.MibaoProtos.MibaoInfoOtherReq;
import qxmobile.protobuf.MibaoProtos.MibaoInfoResp;
import qxmobile.protobuf.MibaoProtos.MibaoLevelupReq;
import qxmobile.protobuf.MibaoProtos.MibaoLevelupResp;
import qxmobile.protobuf.MibaoProtos.MibaoStarUpReq;
import qxmobile.protobuf.MibaoProtos.MibaoStarUpResp;
import qxmobile.protobuf.MoBaiProto.MoBaiInfo;
import qxmobile.protobuf.MoBaiProto.MoBaiReq;
import qxmobile.protobuf.NAccountProtos.NAccount;
import qxmobile.protobuf.NActionResultProtos.NActionResult;
import qxmobile.protobuf.NAfterCombatProtos.NAfterCombat;
import qxmobile.protobuf.NCRCityProtos.NCRCity;
import qxmobile.protobuf.NCRCitysProtos.NCRCitys;
import qxmobile.protobuf.NCheckReportProtos.NCheckReport;
import qxmobile.protobuf.NCityStateMapProtos.NCityStateMapList;
import qxmobile.protobuf.NCityUserListProtos.NCityUserChange;
import qxmobile.protobuf.NCityUserListProtos.NCityUserList;
import qxmobile.protobuf.NCountryInfoProtos.NCountryInfo;
import qxmobile.protobuf.NCountryInfoProtos.NCountryInfoList;
import qxmobile.protobuf.NPersonalAwardProtos.NPersonalAwardList;
import qxmobile.protobuf.NReportProtos.NReport;
import qxmobile.protobuf.NRequestAwardProtos.NRequestAward;
import qxmobile.protobuf.NUserActionProtos.NUserAction;
import qxmobile.protobuf.NUserAttackProtos.NUserAttack;
import qxmobile.protobuf.NUserMoveProtos.NUserMove;
import qxmobile.protobuf.NationalCityProtos.NationalCity;
import qxmobile.protobuf.NationalCityProtos.NationalCityList;
import qxmobile.protobuf.NationalScheduleProtos.NationalSchedule;
import qxmobile.protobuf.NationalScheduleProtos.NationalScheduleList;
import qxmobile.protobuf.NationalWarInfoProtos.NationalWarInfo;
import qxmobile.protobuf.NationalWarInfoProtos.NationalWarInfoList;
import qxmobile.protobuf.NoticeProtos.GetVersionNoticeResp;
import qxmobile.protobuf.PawnShop.PawnShopGoodsSell;
import qxmobile.protobuf.PawnShop.PawnshopGoodsBuy;
import qxmobile.protobuf.PawnShop.PawnshopGoodsBuyResp;
import qxmobile.protobuf.PawnShop.PawnshopGoodsList;
import qxmobile.protobuf.PawnShop.PawnshopRefeshResp;
import qxmobile.protobuf.PkRecordMessage.PkRecordList;
import qxmobile.protobuf.PlayerData.PlayerState;
import qxmobile.protobuf.Prompt.PromptActionReq;
import qxmobile.protobuf.Prompt.PromptActionResp;
import qxmobile.protobuf.*;
import qxmobile.protobuf.PveLevel.BuZhenReport;
import qxmobile.protobuf.PveLevel.GetPveStarAward;
import qxmobile.protobuf.PveLevel.GuanQiaInfo;
import qxmobile.protobuf.PveLevel.GuanQiaInfoRequest;
import qxmobile.protobuf.PveLevel.GuanQiaMaxId;
import qxmobile.protobuf.PveLevel.MibaoSelect;
import qxmobile.protobuf.PveLevel.MibaoSelectResp;
import qxmobile.protobuf.PveLevel.PveBattleOver;
import qxmobile.protobuf.PveLevel.PvePageReq;
import qxmobile.protobuf.PveLevel.PveSaoDangReq;
import qxmobile.protobuf.PveLevel.PveSaoDangRet;
import qxmobile.protobuf.PveLevel.PveStarAwards;
import qxmobile.protobuf.PveLevel.PveStarGetSuccess;
import qxmobile.protobuf.PveLevel.ResetCQTimesBack;
import qxmobile.protobuf.PveLevel.ResetCQTimesReq;
import qxmobile.protobuf.PveLevel.YuanZhuListReturn;
import qxmobile.protobuf.PvpProto.BaiZhanInfoResp;
import qxmobile.protobuf.PvpProto.BaiZhanResult;
import qxmobile.protobuf.PvpProto.BaiZhanResultResp;
import qxmobile.protobuf.PvpProto.ChallengeReq;
import qxmobile.protobuf.PvpProto.ChallengeResp;
import qxmobile.protobuf.PvpProto.ConfirmExecuteReq;
import qxmobile.protobuf.PvpProto.ConfirmExecuteResp;
import qxmobile.protobuf.PvpProto.PlayerStateReq;
import qxmobile.protobuf.PvpProto.PlayerStateResp;
import qxmobile.protobuf.PvpProto.ZhandouRecordResp;
import qxmobile.protobuf.Ranking.AlliancePlayerReq;
import qxmobile.protobuf.Ranking.AlliancePlayerResp;
import qxmobile.protobuf.Ranking.GetRankReq;
import qxmobile.protobuf.Ranking.GetRankResp;
import qxmobile.protobuf.Ranking.RankingReq;
import qxmobile.protobuf.Ranking.RankingResp;
import qxmobile.protobuf.SMessageProtos.SMessage;
import qxmobile.protobuf.Scene.EnterFightScene;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.EnterSceneConfirm;
import qxmobile.protobuf.Scene.ExitFightScene;
import qxmobile.protobuf.Scene.ExitScene;
import qxmobile.protobuf.Scene.SpriteMove;
import qxmobile.protobuf.Settings.ChangeGuojiaReq;
import qxmobile.protobuf.Settings.ChangeName;
import qxmobile.protobuf.Settings.ChangeNameBack;
import qxmobile.protobuf.Settings.ConfGet;
import qxmobile.protobuf.Settings.ConfSave;
import qxmobile.protobuf.Shop.BuyGoodReq;
import qxmobile.protobuf.Shop.BuyGoodResp;
import qxmobile.protobuf.Shop.BuyMibaoPointResp;
import qxmobile.protobuf.Shop.BuyResourceInfosResp;
import qxmobile.protobuf.Shop.BuyTongbiResp;
import qxmobile.protobuf.Shop.PurchaseFail;
import qxmobile.protobuf.Shop.ShopReq;
import qxmobile.protobuf.Shop.ShopResp;
import qxmobile.protobuf.SoundData.PlayerSound;
import qxmobile.protobuf.TimeWorkerProtos.TimeWorkerRequest;
import qxmobile.protobuf.TimeWorkerProtos.TimeWorkerResponse;
import qxmobile.protobuf.UnionListInitProto.FriendListInit;
import qxmobile.protobuf.UnionListInitProto.FriendListInitReq;
import qxmobile.protobuf.UnionListInitProto.UnionAdvance;
import qxmobile.protobuf.UnionListInitProto.UnionAdvanceReq;
import qxmobile.protobuf.UnionListInitProto.UnionAgreeInvite;
import qxmobile.protobuf.UnionListInitProto.UnionAgreeInviteReq;
import qxmobile.protobuf.UnionListInitProto.UnionApllyJoin;
import qxmobile.protobuf.UnionListInitProto.UnionApllyJoinReq;
import qxmobile.protobuf.UnionListInitProto.UnionDemotion;
import qxmobile.protobuf.UnionListInitProto.UnionDemotionReq;
import qxmobile.protobuf.UnionListInitProto.UnionDetailInitReq;
import qxmobile.protobuf.UnionListInitProto.UnionDetailtInit;
import qxmobile.protobuf.UnionListInitProto.UnionDismiss;
import qxmobile.protobuf.UnionListInitProto.UnionDismissReq;
import qxmobile.protobuf.UnionListInitProto.UnionInnerEdit;
import qxmobile.protobuf.UnionListInitProto.UnionInnerEditReq;
import qxmobile.protobuf.UnionListInitProto.UnionLevelup;
import qxmobile.protobuf.UnionListInitProto.UnionLevelupReq;
import qxmobile.protobuf.UnionListInitProto.UnionListApply;
import qxmobile.protobuf.UnionListInitProto.UnionListApplyReq;
import qxmobile.protobuf.UnionListInitProto.UnionListCreate;
import qxmobile.protobuf.UnionListInitProto.UnionListCreateReq;
import qxmobile.protobuf.UnionListInitProto.UnionListEdit;
import qxmobile.protobuf.UnionListInitProto.UnionListEditReq;
import qxmobile.protobuf.UnionListInitProto.UnionListInit;
import qxmobile.protobuf.UnionListInitProto.UnionListInitReq;
import qxmobile.protobuf.UnionListInitProto.UnionListInvite;
import qxmobile.protobuf.UnionListInitProto.UnionListInviteReq;
import qxmobile.protobuf.UnionListInitProto.UnionOuterEdit;
import qxmobile.protobuf.UnionListInitProto.UnionOuterEditReq;
import qxmobile.protobuf.UnionListInitProto.UnionQuit;
import qxmobile.protobuf.UnionListInitProto.UnionQuitReq;
import qxmobile.protobuf.UnionListInitProto.UnionRefuseInvite;
import qxmobile.protobuf.UnionListInitProto.UnionRefuseInviteReq;
import qxmobile.protobuf.UnionListInitProto.UnionRemove;
import qxmobile.protobuf.UnionListInitProto.UnionRemoveReq;
import qxmobile.protobuf.UnionListInitProto.UnionTransfer;
import qxmobile.protobuf.UnionListInitProto.UnionTransferReq;
import qxmobile.protobuf.UpActionProto.UpAction_C_getData;
import qxmobile.protobuf.UserEquipProtos.EquipJinJie;
import qxmobile.protobuf.UserEquipProtos.EquipJinJieResp;
import qxmobile.protobuf.UserEquipProtos.EquipStrength4AllResp;
import qxmobile.protobuf.UserEquipProtos.EquipStrengthReq;
import qxmobile.protobuf.UserEquipProtos.EquipStrengthResp;
import qxmobile.protobuf.UserEquipProtos.UserEquipResp;
import qxmobile.protobuf.UserEquipProtos.UserEquipsReq;
import qxmobile.protobuf.UserEquipProtos.XiLianReq;
import qxmobile.protobuf.UserEquipProtos.XiLianRes;
import qxmobile.protobuf.UserEquipProtos.XilianError;
import qxmobile.protobuf.VIP.RechargeReq;
import qxmobile.protobuf.VIP.RechargeResp;
import qxmobile.protobuf.VIP.VipInfoResp;
import qxmobile.protobuf.WuJiangProtos.BuZhenHeroList;
import qxmobile.protobuf.WuJiangProtos.HeroActivatReq;
import qxmobile.protobuf.WuJiangProtos.HeroActivatResp;
import qxmobile.protobuf.WuJiangProtos.HeroDate;
import qxmobile.protobuf.WuJiangProtos.HeroGrowReq;
import qxmobile.protobuf.WuJiangProtos.HeroGrowResp;
import qxmobile.protobuf.WuJiangProtos.HeroInfoReq;
import qxmobile.protobuf.WuJiangProtos.HeroInfoResp;
import qxmobile.protobuf.WuJiangProtos.JingPoRefreshResp;
import qxmobile.protobuf.WuJiangProtos.WuJiangTech;
import qxmobile.protobuf.WuJiangProtos.WuJiangTechLevelup;
import qxmobile.protobuf.WuJiangProtos.WuJiangTechLevelupReq;
import qxmobile.protobuf.WuJiangProtos.WuJiangTechReq;
import qxmobile.protobuf.WuJiangProtos.WuJiangTechSpeedupResp;
import qxmobile.protobuf.XianShi.GainAward;
import qxmobile.protobuf.XianShi.OpenXianShiResp;
import qxmobile.protobuf.XianShi.ReturnAward;
import qxmobile.protobuf.XianShi.XinShouXSActivity;
import qxmobile.protobuf.XianShi.XinShouXianShiInfo;
import qxmobile.protobuf.Yabiao.AnswerYaBiaoHelpReq;
import qxmobile.protobuf.Yabiao.AnswerYaBiaoHelpResp;
import qxmobile.protobuf.Yabiao.AskYaBiaoHelpResp;
import qxmobile.protobuf.Yabiao.BiaoCheState;
import qxmobile.protobuf.Yabiao.BuyCountsReq;
import qxmobile.protobuf.Yabiao.BuyCountsResp;
import qxmobile.protobuf.Yabiao.EnemiesResp;
import qxmobile.protobuf.Yabiao.EnterYaBiaoScene;
import qxmobile.protobuf.Yabiao.HorsePropReq;
import qxmobile.protobuf.Yabiao.HorsePropResp;
import qxmobile.protobuf.Yabiao.HorseType;
import qxmobile.protobuf.Yabiao.JiaSuReq;
import qxmobile.protobuf.Yabiao.JiaSuResp;
import qxmobile.protobuf.Yabiao.JieBiaoResult;
import qxmobile.protobuf.Yabiao.RoomInfo;
import qxmobile.protobuf.Yabiao.SetHorseResult;
import qxmobile.protobuf.Yabiao.TiChuXieZhuResp;
import qxmobile.protobuf.Yabiao.TiChuYBHelpRsq;
import qxmobile.protobuf.Yabiao.XieZhuJunZhuResp;
import qxmobile.protobuf.Yabiao.YBHistoryResp;
import qxmobile.protobuf.Yabiao.YaBiaoHelpResp;
import qxmobile.protobuf.Yabiao.YabiaoJunZhuInfo;
import qxmobile.protobuf.Yabiao.YabiaoJunZhuList;
import qxmobile.protobuf.Yabiao.YabiaoMainInfoResp;
import qxmobile.protobuf.Yabiao.YabiaoMenuResp;
import qxmobile.protobuf.Yabiao.YabiaoResult;
import qxmobile.protobuf.Yabiao.isNew4RecordResp;
import qxmobile.protobuf.YouXiaProtos.YouXiaGuanQiaInfoReq;
import qxmobile.protobuf.YouXiaProtos.YouXiaGuanQiaInfoResp;
import qxmobile.protobuf.YouXiaProtos.YouXiaInfoResp;
import qxmobile.protobuf.YouXiaProtos.YouXiaSaoDangReq;
import qxmobile.protobuf.YouXiaProtos.YouXiaTimesBuyReq;
import qxmobile.protobuf.YouXiaProtos.YouXiaTimesBuyResp;
import qxmobile.protobuf.YouXiaProtos.YouXiaTimesInfoReq;
import qxmobile.protobuf.YouXiaProtos.YouXiaTimesInfoResp;
import qxmobile.protobuf.ZhanDou.BattleYouXiaResultReq;
import qxmobile.protobuf.ZhanDou.HuangYePveOver;
import qxmobile.protobuf.ZhanDou.HuangYePveReq;
import qxmobile.protobuf.ZhanDou.PveZhanDouInitReq;
import qxmobile.protobuf.ZhanDou.PvpZhanDouInitReq;
import qxmobile.protobuf.ZhanDou.YouXiaZhanDouInitReq;
import qxmobile.protobuf.ZhanDou.ZhanDouInitError;
import qxmobile.protobuf.ZhanDou.ZhanDouInitResp;
import qxmobile.protobuf.ZhangHao.CreateRoleRequest;
import qxmobile.protobuf.ZhangHao.CreateRoleResponse;
import qxmobile.protobuf.ZhangHao.LoginReq;
import qxmobile.protobuf.ZhangHao.LoginRet;
import qxmobile.protobuf.ZhangHao.RegReq;
import qxmobile.protobuf.ZhangHao.RegRet;
import qxmobile.protobuf.ZhangHao.RoleNameRequest;
import qxmobile.protobuf.ZhangHao.RoleNameResponse;

import com.manu.dynasty.util.ProtobufUtils;

/**
 * Protocol Definition（协议号定义）
 * @author 康建虎
 */
public class PD {
	/**
	 * 注册已知的协议类型。
	 */
	public static void init(){
		ParsePD.makeMap();
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_GET_UPACTION_DATA), UpAction_C_getData.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_Pai_big_house), ExchangeHouse.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_zlgdlc), InProgress.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_klwhy_1), InProgress.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_klwhy_2), InProgress.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_klwhy_3), InProgress.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_klwhy_4), InProgress.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_klwhy_5), InProgress.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_klwhy_6), InProgress.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_klwhy_7), InProgress.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_klwhy_8), InProgress.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_klwhy_9), InProgress.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_klwhy_10), InProgress.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(S_InitProc), InitProc.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_JIAN_ZHU_UP), ErrorMessage.getDefaultInstance());
		ProtobufUtils.protoClassToIdMap.put(InitProc.class, Integer.valueOf(S_InitProc));
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_XG_TOKEN), ErrorMessage.getDefaultInstance());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(C_TEST_DELAY), ErrorMessage.getDefaultInstance());
		ProtobufUtils.register(ExCanJuanJiangLi.getDefaultInstance(), C_ExCanJuanJiangLi);
		ProtobufUtils.register(ExItemResult.getDefaultInstance(), S_huan_wu_exchange);
		ProtobufUtils.register(ExchangeItem.getDefaultInstance(), C_huan_wu_exchange);
		ProtobufUtils.register(HouseExpInfo.getDefaultInstance(), S_house_exp);
		ProtobufUtils.register(HouseVisitorInfo.getDefaultInstance(), C_GetHouseVInfo);
		ProtobufUtils.register(HouseVisitorInfo.getDefaultInstance(), S_HouseVInfo);
		ProtobufUtils.register(OffVisitorInfo.getDefaultInstance(), C_ShotOffVisitor);
		ProtobufUtils.register(OffVisitorInfo.getDefaultInstance(), S_ShotOffVisitor);
		ProtobufUtils.register(LianMengBoxes.getDefaultInstance(), S_huan_wu_list);
		ProtobufUtils.register(setHuanWu.getDefaultInstance(), C_huan_wu_Oper);
		ProtobufUtils.register(HuanWuInfo.getDefaultInstance(), S_huan_wu_info);
		ProtobufUtils.register(EnterOrExitHouse.getDefaultInstance(), C_EnterOrExitHouse);
		ProtobufUtils.register(SetHouseState.getDefaultInstance(), C_Set_House_state);
		ProtobufUtils.register(AnswerExchange.getDefaultInstance(), C_AnswerExchange);
		ProtobufUtils.register(ApplyInfos.getDefaultInstance(), S_HOUSE_APPLY_LIST);
		ProtobufUtils.register(ExchangeResult.getDefaultInstance(), S_HOUSE_EXCHANGE_RESULT);
		ProtobufUtils.register(ExchangeHouse.getDefaultInstance(), C_HOUSE_EXCHANGE_RQUEST);
		ProtobufUtils.register(ExchangeEHouse.getDefaultInstance(), C_EHOUSE_EXCHANGE_RQUEST);
		ProtobufUtils.register(BatchSimpleInfo.getDefaultInstance(), S_LM_HOUSE_INFO);
		ProtobufUtils.register(ChangeNameBack.getDefaultInstance(), S_change_name);
		ProtobufUtils.register(ChangeName.getDefaultInstance(), C_change_name);
		ProtobufUtils.register(ConfSave.getDefaultInstance(), C_SETTINGS_SAVE);
		ProtobufUtils.register(ConfGet.getDefaultInstance(), S_SETTINGS);
		ProtobufUtils.register(MoBaiReq.getDefaultInstance(), PD.C_MoBai);
		ProtobufUtils.register(MoBaiInfo.getDefaultInstance(), PD.S_MoBai_Info);
		ProtobufUtils.register(YuJueHeChengResult.getDefaultInstance(), S_YuJueHeChengResult);
		ProtobufUtils.register(TaskProgress.getDefaultInstance(), C_TaskProgress);
		ProtobufUtils.register(GetTaskRwardResult.getDefaultInstance(), S_GetTaskRwardResult);
		ProtobufUtils.register(GetTaskReward.getDefaultInstance(), C_GetTaskReward);
		ProtobufUtils.register(TaskSync.getDefaultInstance(), S_TaskSync);
		ProtobufUtils.register(TaskList.getDefaultInstance(), S_TaskList);
		//
		ProtobufUtils.register(BuZhenReport.getDefaultInstance(), C_BuZhen_Report);
		ProtobufUtils.register(MibaoSelect.getDefaultInstance(), C_MIBAO_SELECT);
		ProtobufUtils.register(MibaoSelectResp.getDefaultInstance(), S_MIBAO_SELECT_RESP);
		ProtobufUtils.register(YuanZhuListReturn.getDefaultInstance(), S_YuanJun_List);
		//
		ProtobufUtils.register(GuanQiaInfoRequest.getDefaultInstance(), PVE_GuanQia_Request);
		ProtobufUtils.register(GuanQiaInfo.getDefaultInstance(), PVE_GuanQia_Info);
		ProtobufUtils.register(ResetCQTimesBack.getDefaultInstance(), S_PVE_Reset_CQ);
		ProtobufUtils.register(ResetCQTimesReq.getDefaultInstance(), C_PVE_Reset_CQ);
		ProtobufUtils.register(BuZhenHeroList.getDefaultInstance(), S_BuZhen_Hero_Info);
		ProtobufUtils.register(BuyTimesInfo.getDefaultInstance(), S_BUY_TIMES_INFO);
		ProtobufUtils.register(BattlePvpInitReq.getDefaultInstance(), Battle_Pvp_Init_Req);
		ProtobufUtils.register(PveSaoDangReq.getDefaultInstance(), C_PVE_SAO_DANG);
		ProtobufUtils.register(PveSaoDangRet.getDefaultInstance(), S_PVE_SAO_DANG);
		/****************** 百战千军协议***************/
		ProtobufUtils.register(BaiZhanInfoResp.getDefaultInstance(), BAIZHAN_INFO_RESP);
		ProtobufUtils.register(ChallengeReq.getDefaultInstance(), CHALLENGE_REQ);
		ProtobufUtils.register(ChallengeResp.getDefaultInstance(), CHALLENGE_RESP);
		ProtobufUtils.register(ConfirmExecuteReq.getDefaultInstance(), CONFIRM_EXECUTE_REQ);
		ProtobufUtils.register(ConfirmExecuteResp.getDefaultInstance(), CONFIRM_EXECUTE_RESP);
		ProtobufUtils.register(BaiZhanResult.getDefaultInstance(), BAIZHAN_RESULT);
		ProtobufUtils.register(PlayerStateReq.getDefaultInstance(), PLAYER_STATE_REQ);
		ProtobufUtils.register(PlayerStateResp.getDefaultInstance(), PLAYER_STATE_RESP);
		ProtobufUtils.register(ZhandouRecordResp.getDefaultInstance(), ZHAN_DOU_RECORD_RESP);
		ProtobufUtils.register(BaiZhanResultResp.getDefaultInstance(), BAIZHAN_RESULT_RESP);
		// 百战end
		// 掠夺
		ProtobufUtils.register(LveDuoInfoResp.getDefaultInstance(), LVE_DUO_INFO_RESP);
		ProtobufUtils.register(LveConfirmReq.getDefaultInstance(), LVE_CONFIRM_REQ);
		ProtobufUtils.register(LveConfirmResp.getDefaultInstance(), LVE_CONFIRM_RESP);
		ProtobufUtils.register(LveGoLveDuoReq.getDefaultInstance(), LVE_GO_LVE_DUO_REQ);
		ProtobufUtils.register(LveGoLveDuoResp.getDefaultInstance(), LVE_GO_LVE_DUO_RESP);
		ProtobufUtils.register(LveBattleRecordResp.getDefaultInstance(), LVE_BATTLE_RECORD_RESP);
		ProtobufUtils.prototypeMap.put((int)ZHANDOU_INIT_LVE_DUO_REQ, PvpZhanDouInitReq.getDefaultInstance());
		ProtobufUtils.prototypeMap.put((int)C_USE_CHENG_HAO, TalentUpLevelReq.getDefaultInstance());
		ProtobufUtils.register(LveBattleEndReq.getDefaultInstance(), LVE_BATTLE_END_REQ);
		ProtobufUtils.register(LveNextItemReq.getDefaultInstance(), LVE_NEXT_ITEM_REQ);
		ProtobufUtils.register(LveNextItemResp.getDefaultInstance(), LVE_NEXT_ITEM_RESP);
		ProtobufUtils.register(LveBattleEndResp.getDefaultInstance(), LVE_BATTLE_END_RESP);
		ProtobufUtils.register(LveHelpReq.getDefaultInstance(), LVE_HELP_REQ);
		// 掠夺end
		
		ProtobufUtils.register(BaiZhanResultResp.getDefaultInstance(), BAIZHAN_RESULT_RESP);
		ProtobufUtils.register(GetDailyAward.getDefaultInstance(), C_get_daily_award);
		//
		ProtobufUtils.register(JingMaiRet.getDefaultInstance(), S_JingMai_info);
		ProtobufUtils.register(XueWeiUpReq.getDefaultInstance(), C_JingMai_up);
		ProtobufUtils.register(JingMaiReq.getDefaultInstance(), C_JingMai_info);
		ProtobufUtils.register(XiLianReq.getDefaultInstance(), C_EQUIP_XiLian);
		ProtobufUtils.register(XiLianRes.getDefaultInstance(), S_EQUIP_XiLian);
		ProtobufUtils.register(XilianError.getDefaultInstance(), S_EQUIP_XILIAN_ERROR);
		ProtobufUtils.register(EquipJinJie.getDefaultInstance(), C_EQUIP_JINJIE);
		ProtobufUtils.register(EquipJinJieResp.getDefaultInstance(), S_EQUIP_JINJIE);
		ProtobufUtils.register(EquipDetail.getDefaultInstance(), S_EquipDetail);
		ProtobufUtils.register(EquipDetailReq.getDefaultInstance(), C_EquipDetailReq);
		ProtobufUtils.register(EquipInfoOtherReq.getDefaultInstance(), C_EquipInfoOtherReq);
		ProtobufUtils.register(EquipInfo.getDefaultInstance(), S_EquipInfo);
		ProtobufUtils.register(BagInfo.getDefaultInstance(), S_BagInfo);
		ProtobufUtils.register(EquipAddReq.getDefaultInstance(), C_EquipAdd);
		ProtobufUtils.register(EquipRemoveReq.getDefaultInstance(), C_EquipRemove);
		ProtobufUtils.register(BattleReplayData.getDefaultInstance(), C_Report_battle_replay);
		ProtobufUtils.register(BattleReplayReq.getDefaultInstance(), C_Request_battle_replay);
		ProtobufUtils.register(KeJiShengJiReq.getDefaultInstance(), C_KeJiUp);
		ProtobufUtils.register(KeJiInfoReq.getDefaultInstance(), C_KeJiInfo);
		ProtobufUtils.register(KeJiInfoRet.getDefaultInstance(), S_KeJiInfo);
		ProtobufUtils.register(qxmobile.protobuf.JunZhuProto.JunZhuInfoReq.getDefaultInstance(), JunZhuInfoReq);
		ProtobufUtils.register(qxmobile.protobuf.JunZhuProto.JunZhuInfoRet.getDefaultInstance(), JunZhuInfoRet);
		ProtobufUtils.register(qxmobile.protobuf.JunZhuProto.JunZhuAttPointReq.getDefaultInstance(), JunZhuAttPointReq);
		ProtobufUtils.register(qxmobile.protobuf.JunZhuProto.JunZhuAttPointRet.getDefaultInstance(), JunZhuAttPointRet);
		ProtobufUtils.register(qxmobile.protobuf.JunZhuProto.JunZhuAddPointReq.getDefaultInstance(), JunZhuAddPointReq);
		ProtobufUtils.register(JunZhuInfoSpecifyReq.getDefaultInstance(), JUNZHU_INFO_SPECIFY_REQ);
		
		ProtobufUtils.register(CGetYuYing.getDefaultInstance(), C_get_sound);
		ProtobufUtils.register(PlayerSound.getDefaultInstance(), PLAYER_SOUND_REPORT);
		ProtobufUtils.register(PlayerState.getDefaultInstance(), PLAYER_STATE_REPORT);
		ProtobufUtils.register(SMessage.getDefaultInstance(), S_Message);
		ProtobufUtils.register(ChatPct.getDefaultInstance(), C_Send_Chat);
		ProtobufUtils.protoClassToIdMap.put(ChatPct.getDefaultInstance().getClass(), (int)S_Send_Chat);
		ProtobufUtils.register(CGetChat.getDefaultInstance(), C_Get_Chat_Log);
		ProtobufUtils.register(SChatLogList.getDefaultInstance(), S_Send_Chat_Log);
		ProtobufUtils.register(NAccount.getDefaultInstance(), C_Call_User_Info);
		ProtobufUtils.register(NUserMove.getDefaultInstance(), C_Call_User_Move);
		ProtobufUtils.register(NUserAction.getDefaultInstance(), C_Call_User_Action);
		ProtobufUtils.register(NUserAttack.getDefaultInstance(), C_Call_User_Attack);
		ProtobufUtils.register(NationalWarInfo.getDefaultInstance(), S_Send_User_Info);
		ProtobufUtils.register(NationalWarInfoList.getDefaultInstance(), NationalWarInfoListResId);
		ProtobufUtils.register(NationalSchedule.getDefaultInstance(), NationalScheduleResId);
		ProtobufUtils.register(NationalScheduleList.getDefaultInstance(), NationalScheduleListResId);
		ProtobufUtils.register(NCRCity.getDefaultInstance(), C_Call_City_Info);
		ProtobufUtils.register(NCRCitys.getDefaultInstance(), C_Call_City_List_Info);
		ProtobufUtils.register(NationalCity.getDefaultInstance(), S_Send_City_Info);
		ProtobufUtils.register(NationalCityList.getDefaultInstance(), S_Send_City_List_Info);
		ProtobufUtils.register(NCityUserList.getDefaultInstance(), S_Send_City_UserList);
		ProtobufUtils.register(NCityStateMapList.getDefaultInstance(), S_Send_City_State_Maps);
		ProtobufUtils.register(NCountryInfo.getDefaultInstance(), S_Send_Country);
		ProtobufUtils.register(NCountryInfoList.getDefaultInstance(), S_Send_Country_List);
		ProtobufUtils.register(NAfterCombat.getDefaultInstance(), S_Send_Combat_Result);
		ProtobufUtils.register(NActionResult.getDefaultInstance(), S_Send_Action_Result);
		ProtobufUtils.register(NCityUserChange.getDefaultInstance(), S_Send_City_User_Change);
		ProtobufUtils.register(NReport.getDefaultInstance(), S_Send_Report);
		ProtobufUtils.register(NPersonalAwardList.getDefaultInstance(), S_Send_Personal_Award);
		ProtobufUtils.register(PkRecordList.getDefaultInstance(), S_Send_Combat_Record);
		ProtobufUtils.register(NCheckReport.getDefaultInstance(), C_Call_Check_Report);
		ProtobufUtils.register(NRequestAward.getDefaultInstance(), C_Call_Fetch_Award);
		ProtobufUtils.register(EnterScene.getDefaultInstance(), Enter_Scene);
		ProtobufUtils.prototypeMap.put((int)Enter_HouseScene, EnterScene.getDefaultInstance());
		ProtobufUtils.prototypeMap.put((int)Exit_HouseScene,ExitScene.getDefaultInstance());
		//押镖进入退出场景
		ProtobufUtils.prototypeMap.put((int)Enter_YBScene, EnterScene.getDefaultInstance());
		ProtobufUtils.prototypeMap.put((int)Exit_YBScene,ExitScene.getDefaultInstance());
		ProtobufUtils.prototypeMap.put((int)ENTER_FIGHT_SCENE, EnterScene.getDefaultInstance());
		ProtobufUtils.register(ExitFightScene.getDefaultInstance(), EXIT_FIGHT_SCENE);
		ProtobufUtils.register(EnterFightScene.getDefaultInstance(), ENTER_FIGHT_SCENE_OK);
		ProtobufUtils.register(EnterSceneConfirm.getDefaultInstance(), Enter_Scene_Confirm);
		ProtobufUtils.register(SpriteMove.getDefaultInstance(), Spirite_Move);
		ProtobufUtils.register(ExitScene.getDefaultInstance(), Exit_Scene);
		ProtobufUtils.register(BattlePveInitReq.getDefaultInstance(), Battle_Pve_Init_Req);
		ProtobufUtils.register(Hero.getDefaultInstance(), B_Hero);
		ProtobufUtils.register(Soldier.getDefaultInstance(), B_Soldier);
		ProtobufUtils.register(Troop.getDefaultInstance(), B_Troop);
		ProtobufUtils.register(BattleInit.getDefaultInstance(), Battle_Init);
		ProtobufUtils.register(RegReq.getDefaultInstance(), ACC_REG);
		ProtobufUtils.register(RegRet.getDefaultInstance(), ACC_REG_RET);
		ProtobufUtils.register(LoginReq.getDefaultInstance(), ACC_LOGIN);
		ProtobufUtils.register(LoginRet.getDefaultInstance(), ACC_LOGIN_RET);
		ProtobufUtils.prototypeMap.put((int)channel_LOGIN, LoginReq.getDefaultInstance());
		ProtobufUtils.register(CreateRoleRequest.getDefaultInstance(), CREATE_ROLE_REQUEST);
		ProtobufUtils.register(CreateRoleResponse.getDefaultInstance(), CREATE_ROLE_RESPONSE);
		ProtobufUtils.register(RoleNameRequest.getDefaultInstance(), ROLE_NAME_REQUEST);
		ProtobufUtils.register(RoleNameResponse.getDefaultInstance(), ROLE_NAME_RESPONSE);
		ProtobufUtils.register(PvePageReq.getDefaultInstance(), PVE_PAGE_REQ);
		ProtobufUtils.register(PveLevel.Section.getDefaultInstance(), PVE_PAGE_RET);
		ProtobufUtils.register(AwardItem.getDefaultInstance(), Award_Item);
		ProtobufUtils.register(BattleResult.getDefaultInstance(), BattlePve_Result);
		ProtobufUtils.register(PveBattleOver.getDefaultInstance(), PVE_BATTLE_OVER_REPORT);
		ProtobufUtils.register(GetPveStarAward.getDefaultInstance(), PVE_STAR_REWARD_INFO_REQ);
		ProtobufUtils.register(PveStarAwards.getDefaultInstance(), PVE_STAR_REWARD_INFO_RET);
		ProtobufUtils.register(GetPveStarAward.getDefaultInstance(), PVE_STAR_REWARD_GET);
		ProtobufUtils.register(PveStarGetSuccess.getDefaultInstance(), PVE_STAR_REWARD_GET_RET);
		ProtobufUtils.register(ErrorMessage.getDefaultInstance(),S_ERROR );
		ProtobufUtils.register(EquipStrengthReq.getDefaultInstance(), C_EQUIP_UPGRADE);
		ProtobufUtils.register(EquipStrengthResp.getDefaultInstance(), S_EQUIP_UPGRADE);
		//一键强化返回
		ProtobufUtils.register(EquipStrength4AllResp.getDefaultInstance(), S_EQUIP_UPALLGRADE);
		
		ProtobufUtils.register(UserEquipsReq.getDefaultInstance(), C_EQUIP_LIST);
		ProtobufUtils.register(UserEquipResp.getDefaultInstance(), S_EQUIP_LIST);
		
		// 邮件
		ProtobufUtils.register(EmailListResponse.getDefaultInstance(), S_REQ_MAIL_LIST);
		ProtobufUtils.register(DeleteEmailResp.getDefaultInstance(), S_DELETE_MAIL);
		ProtobufUtils.register(GetRewardRequest.getDefaultInstance(), C_MAIL_GET_REWARD);
		ProtobufUtils.register(GetRewardResponse.getDefaultInstance(), S_MAIL_GET_REWARD);
		ProtobufUtils.register(NewMailResponse.getDefaultInstance(), S_MAIL_NEW);
		ProtobufUtils.register(ReadEmail.getDefaultInstance(), C_READED_EAMIL);
		ProtobufUtils.register(ReadEmailResp.getDefaultInstance(), S_READED_EAMIL);
		ProtobufUtils.register(EmailResponse.getDefaultInstance(), C_EMAIL_RESPONSE);
		ProtobufUtils.register(EmailResponseResult.getDefaultInstance(), S_EMAIL_RESPONSE);
		ProtobufUtils.register(SendEmail.getDefaultInstance(), C_SEND_EAMIL);
		ProtobufUtils.register(SendEmailResp.getDefaultInstance(), S_SEND_EAMIL);
		
		
		ProtobufUtils.register(HeroInfoReq.getDefaultInstance(), HERO_INFO_REQ);
		ProtobufUtils.register(HeroDate.getDefaultInstance(), HERO_DATA);
		ProtobufUtils.register(HeroInfoResp.getDefaultInstance(), HERO_INFO);
		
		ProtobufUtils.register(UnionListInitReq.getDefaultInstance(), GET_UNION_INFO_REQ);
		ProtobufUtils.register(UnionListInit.getDefaultInstance(), GET_UNION_INFO_RESP);
		ProtobufUtils.register(FriendListInitReq.getDefaultInstance(), GET_UNION_FRIEND_INFO_REQ);
		ProtobufUtils.register(FriendListInit.getDefaultInstance(), GET_UNION_FRIEND_RESP);
		ProtobufUtils.register(UnionListEditReq.getDefaultInstance(), UNION_EDIT_REQ);
		ProtobufUtils.register(UnionListEdit.getDefaultInstance(), UNION_EDIT_RESP);
		ProtobufUtils.register(UnionInnerEditReq.getDefaultInstance(), UNION_INNER_EDIT_REQ);
		ProtobufUtils.register(UnionInnerEdit.getDefaultInstance(), UNION_INNER_EDIT_RESP);
		ProtobufUtils.register(UnionOuterEditReq.getDefaultInstance(), UNION_OUTER_EDIT_REQ);
		ProtobufUtils.register(UnionOuterEdit.getDefaultInstance(), UNION_OUTER_EDIT_RESP);
		ProtobufUtils.register(UnionListCreateReq.getDefaultInstance(), CREATE_UNION_REQ);
		ProtobufUtils.register(UnionListCreate.getDefaultInstance(), CREATE_UNION_RESP);
		ProtobufUtils.register(UnionLevelupReq.getDefaultInstance(), UNION_LEVELUP_REQ);
		ProtobufUtils.register(UnionLevelup.getDefaultInstance(), UNION_LEVELUP_RESP);
		ProtobufUtils.register(UnionListApplyReq.getDefaultInstance(), UNION_APPLY_REQ);
		ProtobufUtils.register(UnionListApply.getDefaultInstance(), UNION_APPLY_JION_RESP);
		ProtobufUtils.register(UnionListInviteReq.getDefaultInstance(), UNION_INVITE_REQ);
		ProtobufUtils.register(UnionListInvite.getDefaultInstance(), UNION_INVITE_RESP);
		ProtobufUtils.register(UnionAgreeInviteReq.getDefaultInstance(), UNION_INVITED_AGREE_REQ);
		ProtobufUtils.register(UnionAgreeInvite.getDefaultInstance(), UNION_INVITED_AGREE_RESP);
		ProtobufUtils.register(UnionRefuseInviteReq.getDefaultInstance(), UNION_INVITED_REFUSE_REQ);
		ProtobufUtils.register(UnionRefuseInvite.getDefaultInstance(), UNION_INVITED_REFUSE_RESP);
		ProtobufUtils.register(UnionQuitReq.getDefaultInstance(), UNION_QUIT_REQ);
		ProtobufUtils.register(UnionQuit.getDefaultInstance(), UNION_QUIT_RESP);
		ProtobufUtils.register(UnionDismissReq.getDefaultInstance(), UNION_DISMISS_REQ);
		ProtobufUtils.register(UnionDismiss.getDefaultInstance(), UNION_DISMISS_RESP);
		ProtobufUtils.register(UnionTransferReq.getDefaultInstance(), UNION_TRANSFER_REQ);
		ProtobufUtils.register(UnionTransfer.getDefaultInstance(), UNION_TRANSFER_RESP);
		ProtobufUtils.register(UnionAdvanceReq.getDefaultInstance(), UNION_ADVANCE_REQ);
		ProtobufUtils.register(UnionAdvance.getDefaultInstance(), UNION_ADVANCE_RESP);
		ProtobufUtils.register(UnionDemotionReq.getDefaultInstance(), UNION_DEMOTION_REQ);
		ProtobufUtils.register(UnionDemotion.getDefaultInstance(), UNION_DEMOTION_RESP);
		ProtobufUtils.register(UnionRemoveReq.getDefaultInstance(), UNION_REMOVE_REQ);
		ProtobufUtils.register(UnionRemove.getDefaultInstance(), UNION_REMOVE_RESP);
		ProtobufUtils.register(UnionApllyJoinReq.getDefaultInstance(), UNION_APPLY_JION_REQ);
		ProtobufUtils.register(UnionApllyJoin.getDefaultInstance(), UNION_APPLY_JION_RESP);
		ProtobufUtils.register(UnionDetailInitReq.getDefaultInstance(), UNION_DETAIL_INFO_REQ);
		ProtobufUtils.register(UnionDetailtInit.getDefaultInstance(), UNION_DETAIL_INFO);
		ProtobufUtils.register(WuJiangTechReq.getDefaultInstance(), WUJIANG_TECHINFO_REQ);
		ProtobufUtils.register(WuJiangTech.getDefaultInstance(), WUJIANG_TECHINFO_RESP);
		ProtobufUtils.register(WuJiangTechReq.getDefaultInstance(), WUJIANG_TECHINFO_REQ);
		ProtobufUtils.register(WuJiangTech.getDefaultInstance(), WUJIANG_TECHINFO_RESP);
		ProtobufUtils.register(WuJiangTechLevelupReq.getDefaultInstance(), WUJIANG_TECHLEVELUP_REQ);
		ProtobufUtils.register(WuJiangTechLevelup.getDefaultInstance(), WUJIANG_TECHLEVELUP_RESP);
		ProtobufUtils.register(HeroGrowReq.getDefaultInstance(), WUJIANG_LEVELUP_REQ);
		ProtobufUtils.register(HeroGrowResp.getDefaultInstance(), WUJIANG_LEVELUP_RESP);
		ProtobufUtils.register(BuyCardBagReq.getDefaultInstance(), BUY_CARDBAG_REQ);
		ProtobufUtils.register(BuyCardBagResp.getDefaultInstance(), BUY_CARDBAG_RESP);
		ProtobufUtils.register(OpenCardBagReq.getDefaultInstance(), OPEN_CARDBAG_REQ);
		ProtobufUtils.register(OpenCardBagResp.getDefaultInstance(), OPEN_CARDBAG_RESP);
		ProtobufUtils.register(TimeWorkerRequest.getDefaultInstance(), C_TIMEWORKER_INTERVAL);
		ProtobufUtils.register(TimeWorkerResponse.getDefaultInstance(), S_TIMEWORKER_INTERVAL);
		ProtobufUtils.register(HeroActivatReq.getDefaultInstance(), HERO_ACTIVE_REQ);
		ProtobufUtils.register(HeroActivatResp.getDefaultInstance(), HERO_ACTIVE_RESP);
		ProtobufUtils.register(JingPoRefreshResp.getDefaultInstance(), JINGPO_REFRESH_RESP);
		ProtobufUtils.register(WuJiangTechSpeedupResp.getDefaultInstance(), WUJIANG_TECH_SPEEDUP_RESP);
		ProtobufUtils.register(PveZhanDouInitReq.getDefaultInstance(), ZHANDOU_INIT_PVE_REQ);
		ProtobufUtils.prototypeMap.put((int)C_ZHANDOU_INIT_YB_REQ, PvpZhanDouInitReq.getDefaultInstance());
		ProtobufUtils.register(PvpZhanDouInitReq.getDefaultInstance(), ZHANDOU_INIT_PVP_REQ);
		ProtobufUtils.register(ZhanDouInitResp.getDefaultInstance(), ZHANDOU_INIT_RESP);
		// 成就协议
		ProtobufUtils.register(AcheListResponse.getDefaultInstance(), S_ACHE_LIST_RESP);
		ProtobufUtils.register(AcheGetRewardRequest.getDefaultInstance(), C_ACHE_GET_REWARD_REQ);
		ProtobufUtils.register(AcheGetRewardResponse.getDefaultInstance(), S_ACHE_GET_REWARD_RESP);
		ProtobufUtils.register(AcheFinishInform.getDefaultInstance(), S_ACHE_FINISH_INFORM);
		//  每日任务
		ProtobufUtils.register(DailyTaskListResponse.getDefaultInstance(), S_DAILY_TASK_LIST_RESP);
		ProtobufUtils.register(DailyTaskFinishInform.getDefaultInstance(), S_DAILY_TASK_FINISH_INFORM);
		ProtobufUtils.register(DailyTaskRewardRequest.getDefaultInstance(), C_DAILY_TASK_GET_REWARD_REQ);
		ProtobufUtils.register(DailyTaskRewardResponse.getDefaultInstance(), S_DAILY_TASK_GET_REWARD_RESP);
		
		// 商城
		ProtobufUtils.register(BuyResourceInfosResp.getDefaultInstance(), BUY_RESOURCE_INFOS_RESP);
		ProtobufUtils.register(PurchaseFail.getDefaultInstance(), PURCHASE_FAIL);
		ProtobufUtils.register(BuyTongbiResp.getDefaultInstance(), S_BUY_TongBi);
		ProtobufUtils.register(BuyMibaoPointResp.getDefaultInstance(), S_BUY_MIBAO_POINT_RESP);
		
		// 秘宝
		ProtobufUtils.register(MibaoActivate.getDefaultInstance(), PD.C_MIBAO_ACTIVATE_REQ);
		ProtobufUtils.register(MibaoActivateResp.getDefaultInstance(), PD.S_MIBAO_ACTIVATE_RESP);
		ProtobufUtils.register(MibaoInfoResp.getDefaultInstance(), PD.S_MIBAO_INFO_RESP);
		ProtobufUtils.register(MibaoStarUpReq.getDefaultInstance(), PD.C_MIBAO_STARUP_REQ);
		ProtobufUtils.register(MibaoStarUpResp.getDefaultInstance(), PD.S_MIBAO_STARUP_RESP);
		ProtobufUtils.register(MibaoLevelupReq.getDefaultInstance(), PD.C_MIBAO_LEVELUP_REQ);
		ProtobufUtils.register(MibaoLevelupResp.getDefaultInstance(), PD.S_MIBAO_LEVELUP_RESP);
		ProtobufUtils.register(MibaoInfoOtherReq.getDefaultInstance(), PD.C_MIBAO_INFO_OTHER_REQ);
		ProtobufUtils.register(MiBaoDealSkillReq.getDefaultInstance(), PD.MIBAO_DEAL_SKILL_REQ);
		ProtobufUtils.register(MiBaoDealSkillResp.getDefaultInstance(), PD.MIBAO_DEAL_SKILL_RESP);
//		ProtobufUtils.register(GetFullStarAwardresp.getDefaultInstance(), PD.GET_FULL_STAR_AWARD_RESP);
		
		// 探宝
		ProtobufUtils.register(ExploreInfoResp.getDefaultInstance(), EXPLORE_INFO_RESP);
		ProtobufUtils.register(ExploreReq.getDefaultInstance(), EXPLORE_REQ);
		ProtobufUtils.register(ExploreResp.getDefaultInstance(), EXPLORE_RESP);
		
		// 当铺
		ProtobufUtils.register(PawnshopGoodsBuy.getDefaultInstance(), PAWN_SHOP_GOODS_BUY);
		ProtobufUtils.register(PawnshopGoodsList.getDefaultInstance(), PAWN_SHOP_GOODS_LIST);
		ProtobufUtils.register(PawnShopGoodsSell.getDefaultInstance(), PAWN_SHOP_GOODS_SELL);
		ProtobufUtils.register(PawnshopRefeshResp.getDefaultInstance(), PAWN_SHOP_GOODS_REFRESH_RESP);
		ProtobufUtils.register(PawnshopGoodsBuyResp.getDefaultInstance(), PAWN_SHOP_GOODS_BUY_RESP);
		
		//联盟
		ProtobufUtils.register(AllianceNonResp.getDefaultInstance(), ALLIANCE_NON_RESP);
		ProtobufUtils.register(AllianceHaveResp.getDefaultInstance(), ALLIANCE_HAVE_RESP);
		ProtobufUtils.register(CheckAllianceName.getDefaultInstance(), CHECK_ALLIANCE_NAME);
		ProtobufUtils.register(CheckAllianceNameResp.getDefaultInstance(), CHECK_ALLIANCE_NAME_RESP);
		ProtobufUtils.register(CreateAlliance.getDefaultInstance(), CREATE_ALLIANCE);
		ProtobufUtils.register(CreateAllianceResp.getDefaultInstance(), CREATE_ALLIANCE_RESP);
		ProtobufUtils.register(FindAlliance.getDefaultInstance(), FIND_ALLIANCE);
		ProtobufUtils.register(FindAllianceResp.getDefaultInstance(), FIND_ALLIANCE_RESP);
		ProtobufUtils.register(ApplyAlliance.getDefaultInstance(), APPLY_ALLIANCE);
		ProtobufUtils.register(ApplyAllianceResp.getDefaultInstance(), APPLY_ALLIANCE_RESP);
		ProtobufUtils.register(CancelJoinAlliance.getDefaultInstance(), CANCEL_JOIN_ALLIANCE);
		ProtobufUtils.register(CancelJoinAllianceResp.getDefaultInstance(), CANCEL_JOIN_ALLIANCE_RESP);
		ProtobufUtils.register(ExitAlliance.getDefaultInstance(), EXIT_ALLIANCE);
		ProtobufUtils.register(ExitAllianceResp.getDefaultInstance(), EXIT_ALLIANCE_RESP);
		ProtobufUtils.register(LookMembers.getDefaultInstance(), LOOK_MEMBERS);
		ProtobufUtils.register(LookMembersResp.getDefaultInstance(), LOOK_MEMBERS_RESP);
		ProtobufUtils.register(FireMember.getDefaultInstance(), FIRE_MEMBER);
		ProtobufUtils.register(FireMemberResp.getDefaultInstance(), FIRE_MEMBER_RESP);
		ProtobufUtils.register(UpTitle.getDefaultInstance(), UP_TITLE);
		ProtobufUtils.register(UpTitleResp.getDefaultInstance(), UP_TITLE_RESP);
		ProtobufUtils.register(DownTitle.getDefaultInstance(), DOWN_TITLE);
		ProtobufUtils.register(DownTitleResp.getDefaultInstance(), DOWN_TITLE_RESP);
		ProtobufUtils.register(LookApplicants.getDefaultInstance(), LOOK_APPLICANTS);
		ProtobufUtils.register(LookApplicantsResp.getDefaultInstance(), LOOK_APPLICANTS_RESP);
		ProtobufUtils.register(RefuseApply.getDefaultInstance(), REFUSE_APPLY);
		ProtobufUtils.register(RefuseApplyResp.getDefaultInstance(), REFUSE_APPLY_RESP);
		ProtobufUtils.register(AgreeApply.getDefaultInstance(), AGREE_APPLY);
		ProtobufUtils.register(AgreeApplyResp.getDefaultInstance(), AGREE_APPLY_RESP);
		ProtobufUtils.register(UpdateNotice.getDefaultInstance(), UPDATE_NOTICE);
		ProtobufUtils.register(UpdateNoticeResp.getDefaultInstance(), UPDATE_NOTICE_RESP);
		ProtobufUtils.register(DismissAlliance.getDefaultInstance(), DISMISS_ALLIANCE);
		ProtobufUtils.register(OpenApply.getDefaultInstance(), OPEN_APPLY);
		ProtobufUtils.register(OpenApplyResp.getDefaultInstance(), OPEN_APPLY_RESP);
		ProtobufUtils.register(CloseApply.getDefaultInstance(), CLOSE_APPLY);
		ProtobufUtils.register(TransferAlliance.getDefaultInstance(), TRANSFER_ALLIANCE);
		ProtobufUtils.register(TransferAllianceResp.getDefaultInstance(), TRANSFER_ALLIANCE_RESP);
		ProtobufUtils.register(MengZhuApplyResp.getDefaultInstance(), MENGZHU_APPLY_RESP);
		ProtobufUtils.register(MengZhuVote.getDefaultInstance(), MENGZHU_VOTE);
		ProtobufUtils.register(MengZhuVoteResp.getDefaultInstance(), MENGZHU_VOTE_RESP);
		ProtobufUtils.register(GiveUpVoteResp.getDefaultInstance(), GIVEUP_VOTE_RESP);
		ProtobufUtils.register(immediatelyJoin.getDefaultInstance(), IMMEDIATELY_JOIN);
		ProtobufUtils.register(immediatelyJoinResp.getDefaultInstance(), IMMEDIATELY_JOIN_RESP);
		ProtobufUtils.register(JoinToBlacklist.getDefaultInstance(), C_JOIN_BLACKLIST);
		//ProtobufUtils.register(BlacklistResp.getDefaultInstance(), S_JOIN_BLACKLIST_RESP);
		//ProtobufUtils.register(GetBlacklistResp.getDefaultInstance(), S_GET_BALCKLIST);
		ProtobufUtils.register(CancelBlack.getDefaultInstance(), C_CANCEL_BALCK);
		//ProtobufUtils.register(BlacklistResp.getDefaultInstance(), S_CANCEL_BALCK);
		ProtobufUtils.register(DonateHuFu.getDefaultInstance(), ALLIANCE_HUFU_DONATE);
		ProtobufUtils.register(DonateHuFuResp.getDefaultInstance(), ALLIANCE_HUFU_DONATE_RESP);
		ProtobufUtils.register(EventListResp.getDefaultInstance(), ALLINACE_EVENT_RESP);
		
		//荒野求生
//		ProtobufUtils.register(OpenHuangYe.getDefaultInstance(), C_OPEN_HUANGYE);
		ProtobufUtils.register(OpenHuangYeResp.getDefaultInstance(), S_OPEN_HUANGYE);
//		ProtobufUtils.register(OpenFog.getDefaultInstance(), C_OPEN_FOG);
//		ProtobufUtils.register(OpenFogResp.getDefaultInstance(), S_OPEN_FOG);
		ProtobufUtils.register(OpenHuangYeTreasure.getDefaultInstance(), C_OPEN_TREASURE);
		ProtobufUtils.register(OpenHuangYeTreasureResp.getDefaultInstance(), S_OPEN_TREASURE);
//		ProtobufUtils.register(ReqRewardStore.getDefaultInstance(), C_REQ_REWARD_STORE);
//		ProtobufUtils.register(ReqRewardStoreResp.getDefaultInstance(), S_REQ_REWARD_STORE);
//		ProtobufUtils.register(ApplyReward.getDefaultInstance(), C_APPLY_REWARD);
//		ProtobufUtils.register(ApplyRewardResp.getDefaultInstance(), S_APPLY_REWARD);
//		ProtobufUtils.register(CancelApplyReward.getDefaultInstance(), C_CANCEL_APPLY_REWARD);
//		ProtobufUtils.register(CancelApplyRewardResp.getDefaultInstance(), S_CANCEL_APPLY_REWARD);
//		ProtobufUtils.register(GiveReward.getDefaultInstance(), C_GIVE_REWARD);
//		ProtobufUtils.register(GiveRewardResp.getDefaultInstance(), S_GIVE_REWARD);
		ProtobufUtils.register(HuangYePveReq.getDefaultInstance(), C_HUANGYE_PVE);
		ProtobufUtils.register(HuangYePveReq.getDefaultInstance(), C_HUANGYE_PVE);
		ProtobufUtils.register(HYTreasureBattle.getDefaultInstance(), C_HYTREASURE_BATTLE);
		ProtobufUtils.register(HYTreasureBattleResp.getDefaultInstance(), S_HYTREASURE_BATTLE_RESP);
		ProtobufUtils.register(HuangYePveOver.getDefaultInstance(), C_HUANGYE_PVE_OVER);
//		ProtobufUtils.register(BattleResouceReq.getDefaultInstance(), C_HYRESOURCE_BATTLE);
//		ProtobufUtils.register(BattleResouceResp.getDefaultInstance(), S_HYRESOURCE_BATTLE_RESP);
//		ProtobufUtils.register(HuangYePvpReq.getDefaultInstance(), C_HUANGYE_PVP);
//		ProtobufUtils.register(HuangYePvpOver.getDefaultInstance(), C_HUANGYE_PVP_OVER);
//		ProtobufUtils.register(BattleResultHYPvp.getDefaultInstance(), S_HUANGYE_PVP_OVER_RESP);
//		ProtobufUtils.register(ResourceChange.getDefaultInstance(), C_HYRESOURCE_CHANGE);
//		ProtobufUtils.register(ResourceChangeResp.getDefaultInstance(), S_HYRESOURCE_CHANGE_RESP);
		ProtobufUtils.register(ShopReq.getDefaultInstance(), HY_SHOP_REQ);
		ProtobufUtils.register(ShopResp.getDefaultInstance(), HY_SHOP_RESP);
		ProtobufUtils.register(BuyGoodReq.getDefaultInstance(), HY_BUY_GOOD_REQ);
		ProtobufUtils.register(BuyGoodResp.getDefaultInstance(), HY_BUY_GOOD_RESP);
		ProtobufUtils.register(ActiveTreasureReq.getDefaultInstance(), ACTIVE_TREASURE_REQ);
		ProtobufUtils.register(ActiveTreasureResp.getDefaultInstance(), ACTIVE_TREASURE_RESP);
		ProtobufUtils.register(MaxDamageRankReq.getDefaultInstance(), MAX_DAMAGE_RANK_REQ);
		ProtobufUtils.register(MaxDamageRankResp.getDefaultInstance(), MAX_DAMAGE_RANK_RESP);
		ProtobufUtils.register(HyBuyBattleTimesResp.getDefaultInstance(), HY_BUY_BATTLE_TIMES_RESP);

		/*
		 * 天赋
		 */
		ProtobufUtils.register(TalentInfoResp.getDefaultInstance(), TALENT_INFO_RESP);
		ProtobufUtils.register(TalentUpLevelReq.getDefaultInstance(), TALENT_UP_LEVEL_REQ);
		ProtobufUtils.register(TalentUpLevelResp.getDefaultInstance(), TALENT_UP_LEVEL_RESP);
		//排行榜
		ProtobufUtils.register(RankingReq.getDefaultInstance(), RANKING_REP);
		ProtobufUtils.register(RankingResp.getDefaultInstance(), RANKING_RESP);
		ProtobufUtils.register(AlliancePlayerReq.getDefaultInstance(), RANKING_ALLIANCE_PLAYER_REQ);
		ProtobufUtils.register(AlliancePlayerResp.getDefaultInstance(), RANKING_ALLIANCE_PLAYER_RESP);
		ProtobufUtils.register(GetRankReq.getDefaultInstance(), GET_RANK_REQ);
		ProtobufUtils.register(GetRankResp.getDefaultInstance(), GET_RANK_RESP);
	
		// 充值
		ProtobufUtils.register(RechargeReq.getDefaultInstance(), C_RECHARGE_REQ);
		ProtobufUtils.register(RechargeResp.getDefaultInstance(), S_RECHARGE_RESP);
		ProtobufUtils.register(VipInfoResp.getDefaultInstance(), S_VIPINFO_RESP);
		
		ProtobufUtils.register(PveMiBaoZhanLi.getDefaultInstance(), S_PVE_MIBAO_ZHANLI);

		// 好友
		ProtobufUtils.register(AddFriendReq.getDefaultInstance(), C_FRIEND_ADD_REQ);
		ProtobufUtils.register(RemoveFriendReq.getDefaultInstance(), C_FRIEND_REMOVE_REQ);
		ProtobufUtils.register(GetFriendListReq.getDefaultInstance(), C_FRIEND_REQ);
		
		ProtobufUtils.register(ChangeGuojiaReq.getDefaultInstance(),C_ZHUANGGUO_REQ);
		//押镖
		ProtobufUtils.register(YabiaoMainInfoResp.getDefaultInstance(), S_YABIAO_INFO_RESP);
		ProtobufUtils.register(YabiaoMenuResp.getDefaultInstance(), S_YABIAO_MENU_RESP);
		ProtobufUtils.register(HorseType.getDefaultInstance(), C_SETHORSE_REQ);
		ProtobufUtils.register(SetHorseResult.getDefaultInstance(), S_SETHORSE_RESP);
		ProtobufUtils.register(RoomInfo.getDefaultInstance(), C_BIAOCHE_INFO);
		ProtobufUtils.register(YabiaoJunZhuList.getDefaultInstance(), S_BIAOCHE_INFO_RESP);
		ProtobufUtils.register(BiaoCheState.getDefaultInstance(), S_BIAOCHE_STATE);
		ProtobufUtils.register(YabiaoResult.getDefaultInstance(), S_YABIAO_RESP);
		ProtobufUtils.register(JieBiaoResult.getDefaultInstance(), C_YABIAO_RESULT);
		ProtobufUtils.register(ZhanDouInitError.getDefaultInstance(), S_ZHANDOU_INIT_ERROR);
		ProtobufUtils.register(EnterYaBiaoScene.getDefaultInstance(), C_ENTER_YABIAOSCENE);
		ProtobufUtils.register(HorseType.getDefaultInstance(), C_SETHORSE_REQ);
		ProtobufUtils.register(YabiaoJunZhuInfo.getDefaultInstance(), S_YABIAO_ENTER_RESP);
		ProtobufUtils.register(EnemiesResp.getDefaultInstance(), S_YABIAO_ENEMY_RESP);
		ProtobufUtils.register(BuyCountsReq.getDefaultInstance(), C_YABIAO_BUY_RSQ);
		ProtobufUtils.register(BuyCountsResp.getDefaultInstance(), S_YABIAO_BUY_RESP);
		ProtobufUtils.register(YBHistoryResp.getDefaultInstance(), S_YABIAO_HISTORY_RESP);
		ProtobufUtils.register(JiaSuReq.getDefaultInstance(), C_CARTJIASU_REQ);
		ProtobufUtils.register(JiaSuResp.getDefaultInstance(), S_CARTJIASU_RESP);
		ProtobufUtils.register(PlayerReviveRequest.getDefaultInstance(), PLAYER_REVIVE_REQUEST);
		//押镖协助
		ProtobufUtils.register(YaBiaoHelpResp.getDefaultInstance(), S_YABIAO_HELP_RESP);
		ProtobufUtils.register(AnswerYaBiaoHelpReq.getDefaultInstance(), C_ANSWER_YBHELP_RSQ);
		ProtobufUtils.register(AnswerYaBiaoHelpResp.getDefaultInstance(), S_ANSWER_YBHELP_RESP);
		ProtobufUtils.register(TiChuYBHelpRsq.getDefaultInstance(), C_TICHU_YBHELP_RSQ);
		ProtobufUtils.register(AskYaBiaoHelpResp.getDefaultInstance(), S_ASK_YABIAO_HELP_RESP);
		ProtobufUtils.register(TiChuXieZhuResp.getDefaultInstance(), S_TICHU_YBHELPXZ_RESP);
		ProtobufUtils.register(isNew4RecordResp.getDefaultInstance(), S_PUSH_YBRECORD_RESP);
		ProtobufUtils.register(HorsePropReq.getDefaultInstance(), C_BUYHORSEBUFF_REQ);
		ProtobufUtils.register(HorsePropResp.getDefaultInstance(), S_BUYHORSEBUFF_RESP);
		ProtobufUtils.register(XieZhuJunZhuResp.getDefaultInstance(), S_YABIAO_XIEZHUS_RESP);
		
		// 游侠
		ProtobufUtils.register(YouXiaZhanDouInitReq.getDefaultInstance(), C_YOUXIA_INIT_REQ);
		ProtobufUtils.register(BattleYouXiaResultReq.getDefaultInstance(), C_YOUXIA_BATTLE_OVER_REQ);
		ProtobufUtils.register(YouXiaInfoResp.getDefaultInstance(), S_YOUXIA_INFO_RESP);
		ProtobufUtils.register(YouXiaTimesInfoReq.getDefaultInstance(), C_YOUXIA_TIMES_INFO_REQ);
		ProtobufUtils.register(YouXiaTimesInfoResp.getDefaultInstance(), S_YOUXIA_TIMES_INFO_RESP);
		ProtobufUtils.register(YouXiaTimesBuyReq.getDefaultInstance(), C_YOUXIA_TIMES_BUY_REQ);
		ProtobufUtils.register(YouXiaTimesBuyResp.getDefaultInstance(), S_YOUXIA_TIMES_BUY_RESP);
		ProtobufUtils.register(YouXiaSaoDangReq.getDefaultInstance(), C_YOUXIA_SAO_DANG_REQ);
		ProtobufUtils.register(YouXiaGuanQiaInfoReq.getDefaultInstance(), C_YOUXIA_GUANQIA_REQ);
		ProtobufUtils.register(YouXiaGuanQiaInfoResp.getDefaultInstance(), S_YOUXIA_GUANQIA_RESP);
		
		//限时活动
		ProtobufUtils.register(OpenXianShiResp.getDefaultInstance(), S_XIANSHI_RESP);
		ProtobufUtils.register(GainAward.getDefaultInstance(),C_XINSHOU_XIANSHI_AWARD_REQ );
		ProtobufUtils.register(ReturnAward.getDefaultInstance(), S_XINSHOU_XIANSHI_AWARD_RESP);
		ProtobufUtils.register(XinShouXSActivity.getDefaultInstance(), C_XINSHOU_XIANSHI_INFO_REQ);
		ProtobufUtils.register(XinShouXianShiInfo.getDefaultInstance(), S_XINSHOU_XIANSHI_INFO_RESP);
		ProtobufUtils.prototypeMap.put((int)C_XIANSHI_AWARD_REQ,GainAward.getDefaultInstance());
		ProtobufUtils.prototypeMap.put((int)S_XIANSHI_AWARD_RESP ,ReturnAward.getDefaultInstance());
		ProtobufUtils.prototypeMap.put((int)C_XIANSHI_INFO_REQ,XinShouXSActivity.getDefaultInstance());
		ProtobufUtils.prototypeMap.put((int)S_XIANSHI_INFO_RESP,XinShouXianShiInfo.getDefaultInstance());
		
		// 公告
		ProtobufUtils.register(GetVersionNoticeResp.getDefaultInstance(), S_YOUXIA_TIMES_BUY_RESP);
		ProtobufUtils.register(GuanQiaMaxId.getDefaultInstance(), PVE_MAX_ID_RESP);
		
		// 国家主页
		ProtobufUtils.register(JuanXianGongJinResp.getDefaultInstance(), S_GET_JUANXIAN_GONGJIN_RESP);
		ProtobufUtils.register(JuanXianDayAwardResp.getDefaultInstance(), S_GET_JUANXIAN_DAYAWARD_RESP);
		ProtobufUtils.register(GuoJiaMainInfoResp.getDefaultInstance(), GUO_JIA_MAIN_INFO_RESP);
//		ProtobufUtils.register(IsCanJuanXianResp.getDefaultInstance(), S_ISCAN_JUANXIAN_RESP);废弃
		
		// 符文
		ProtobufUtils.register(QueryFuwenResp.getDefaultInstance(), S_QUERY_FUWEN_RESP);
		ProtobufUtils.register(OperateFuwenReq.getDefaultInstance(), C_OPERATE_FUWEN_REQ);
		ProtobufUtils.register(FuwenResp.getDefaultInstance(), S_OPERATE_FUWEN_RESP);
		
		// CDKEY
		ProtobufUtils.register(GetCDKeyAwardReq.getDefaultInstance(), C_GET_CDKETY_AWARD_REQ);
		ProtobufUtils.register(GetCDKeyAwardResp.getDefaultInstance(), S_GET_CDKETY_AWARD_RESP);
		
		// 速报
		ProtobufUtils.register(PromptActionReq.getDefaultInstance(), Prompt_Action_Req);
		ProtobufUtils.register(PromptActionResp.getDefaultInstance(), Prompt_Action_Resp);
		// 技能培养
		ProtobufUtils.register(GetJiNengPeiYangQuality.getDefaultInstance(),S_GET_JINENG_PEIYANG_QUALITY_RESP);
		ProtobufUtils.register(UpgradeJiNengReq.getDefaultInstance(),C_UPGRADE_JINENG_REQ);
		ProtobufUtils.register(UpgradeJiNengResp.getDefaultInstance(),S_UPGRADE_JINENG_RESP);
		
		ProtobufUtils.register(RequestFightInfoResp.getDefaultInstance(), ALLIANCE_FIGHT_INFO_RESP);
		ProtobufUtils.register(ApplyFightResp.getDefaultInstance(), ALLIANCE_FIGHT_APPLY_RESP);
		ProtobufUtils.register(FightAttackReq.getDefaultInstance(), FIGHT_ATTACK_REQ);
		ProtobufUtils.register(FightAttackResp.getDefaultInstance(), FIGHT_ATTACK_RESP);
		ProtobufUtils.register(BattlefieldInfoResp.getDefaultInstance(), ALLIANCE_BATTLE_FIELD_RESP);
		ProtobufUtils.register(PlayerDeadNotify.getDefaultInstance(), ALLIANCE_FIGHT_PLAYER_DEAD);
		ProtobufUtils.register(PlayerReviveNotify.getDefaultInstance(), ALLIANCE_FIGHT_PLAYER_REVIVE);
		ProtobufUtils.register(FightHistoryResp.getDefaultInstance(), ALLIANCE_FIGHT_HISTORY_RESP);
		ProtobufUtils.register(BattlefieldInfoNotify.getDefaultInstance(), ALLIANCE_BATTLE_FIELD_NOTIFY);
		ProtobufUtils.register(BattleResultAllianceFight.getDefaultInstance(), ALLIANCE_BATTLE_RESULT);
		ProtobufUtils.register(FightLasttimeRankResp.getDefaultInstance(), ALLIANCE_FIGTH_LASTTIME_RANK_RESP);
		ProtobufUtils.register(BufferInfo.getDefaultInstance(), BUFFER_INFO);
		
	}
	
	public static final short DEBUG_PROTO_WITHOUT_CONTENT		= 100;
	public static final short DEBUG_PROTO_WITHOUT_CONTENT_RET	= 101;
	
	public static final short C_TEST_DELAY = 110;
	public static final short S_TEST_DELAY = 111;
	
	public static final short C_DROP_CONN = 120;//客户端主动断开连接
	
	public static final short C_InitProc = 301;
	public static final short S_InitProc = 302;
	public static final short C_zlgdlc = 303;//客户端终止加密过程
	public static final short S_zlgdlc = 305;//服务器告知客户端过程有误。
	
	////战斗过程加密协议号
	public static final short C_klwhy_1 = 502;
	public static final short C_klwhy_2 = 503;
	public static final short C_klwhy_3 = 504;
	public static final short C_klwhy_4 = 505;
	public static final short C_klwhy_5 = 506;
	public static final short C_klwhy_6 = 507;
	public static final short C_klwhy_7 = 508;
	public static final short C_klwhy_8 = 509;
	public static final short C_klwhy_9 = 512;
	public static final short C_klwhy_10 = 522;
	public static final short[] C_RND_PROT = {C_klwhy_1,C_klwhy_2,C_klwhy_3,C_klwhy_4,C_klwhy_5,C_klwhy_6,
		C_klwhy_7,C_klwhy_8,C_klwhy_9,C_klwhy_10};
	//战斗过程加密协议号
	
	public static final short TEST_CONN = 10001;
	public static final short S_Broadcast = 10003;
	/**
	 * 错误返回信息
	 */
	public static final short S_ERROR = 10010;
	public static final short C_XG_TOKEN = 10101;
	public static final short S_Message = 20000;
	/**
	 * 客户端发送聊天
	 */
	public static final short C_Send_Chat = 20001;
	/**
	 * 服务器通知客户端聊天消息
	 */
	public static final short S_Send_Chat = 20002;
	
	/**
	 * 获取聊天记录。
	 */
	public static final short C_Get_Chat_Log = 20003;
	public static final short S_Send_Chat_Log = 20004;
	
	
	/**
	 * 国战 Socket C 获取 用户信息
	 */
	public static final short C_Call_User_Info = 21101;
	
	/**
	 * 国战 Socket S 发送  用户信息
	 */
	public static final short S_Send_User_Info = 21001;
	
	/**
	 * 国战 Socket C 请求 城池信息 
	 */
	public static final short C_Call_City_Info = 21102;
	
	/**
	 * 国战 Socket C 请求 城池信息 
	 */
	public static final short C_Call_City_List_Info = 21103;
	
	/**
	 * 国战 Socket C 玩家移动
	 */
	public static final short C_Call_User_Move = 21104;
	
	/**
	 * 国战 Socket C 玩家发起攻占、修城动作
	 */
	public static final short C_Call_User_Action = 21105;
	
	/**
	 * 国战 Socket C 玩家发起攻击
	 */
	public static final short C_Call_User_Attack = 21106;
	
	/**
	 * 国战 Socket C 玩家请求查看战报或离开
	 */
	public static final short C_Call_Check_Report = 21107;
	
	/**
	 * 国战 Socket C 玩家请求获取个人奖励
	 */
	public static final short C_Call_Fetch_Award = 21108;
	
	/**
	 * 国战 Socket S 发送  城池信息
	 */
	public static final short S_Send_City_Info = 21003;
	
	/**
	 * 国战 Socket S 发送  城池列表信息
	 */
	public static final short S_Send_City_List_Info = 21007;
	
	/**
	 * 国战 Socket S 发送 城池中用户列表
	 */
	public static final short S_Send_City_UserList = 21006;
	
	/**
	 * 国战 Socket S 发送 城池状态Map列表
	 */
	public static final short S_Send_City_State_Maps = 21008;
	
	/**
	 * 国战 Socket S 发送 国家信息
	 */
	public static final short S_Send_Country = 21009;
	
	/**
	 * 国战 Socket S 发送 国家信息列表
	 */
	public static final short S_Send_Country_List = 21010;
	
	/**
	 * 国战 Socket S 发送 国战战斗结果
	 */
	public static final short S_Send_Combat_Result = 21011;
	
	/**
	 * 国战 Socket S 发送 玩家动作执行结果
	 */
	public static final short S_Send_Action_Result = 21012;
	
	/**
	 * 国战 Socket S 发送 战报记录
	 */
	public static final short S_Send_Report = 21013;
	
	/**
	 * 国战 Socket S 发送 城池玩家变动信息
	 */
	public static final short S_Send_City_User_Change = 21014;
	
	/**
	 * 国战 Socket S 发送 领取到的奖励
	 */
	public static final short S_Send_Personal_Award = 21015;
	/**
	 * 国战 Socket S 发送 个人战报
	 */
	public static final short S_Send_Combat_Record = 21016;
	
	public static final short NationalWarInfoListResId = 21002;
	public static final short NationalScheduleResId = 21004;
	public static final short NationalScheduleListResId = 21005;
	
	/**
	 * 场景相关协议号
	 */
	public static final short Enter_Scene = 22000;
	public static final short Enter_Scene_Confirm = 22001;
	public static final short Spirite_Move = 22002;
	public static final short Exit_Scene = 22003;
	public static final short Enter_HouseScene = 22004;
	public static final short Exit_HouseScene = 22005;
	public static final short Enter_YBScene = 22009;
	public static final short Exit_YBScene = 22010;
	
	public static final short S_HEAD_STRING = 22101;
	
	/**
	 * pve相关协议号
	 */
	public static final short Battle_Pve_Init_Req = 23000;
	public static final short B_Hero = 23001;
	public static final short B_Soldier = 23002;
	public static final short B_Troop = 23003;
	public static final short Battle_Init = 23004;
	
	public static final short Battle_Pvp_Init_Req = 23005;
	public static final short Battle_Pvp_Init = 23006;
	/** 请求过关斩将最大关卡id */
	public static final short PVE_MAX_ID_REQ = 23007;
	/** 返回过关斩将最大关卡id */
	public static final short PVE_MAX_ID_RESP = 23008;
	
	//账号协议
	public static final short ACC_REG = 23101;
	public static final short ACC_REG_RET = 23102;
	public static final short ACC_LOGIN = 23103;
	public static final short ACC_LOGIN_RET = 23104;
	public static final short CREATE_ROLE_REQUEST = 23105;
	public static final short CREATE_ROLE_RESPONSE = 23106;
	public static final short ROLE_NAME_REQUEST = 23107;
	public static final short ROLE_NAME_RESPONSE = 23108;
	public static final short S_ACC_login_kick = 23110;
	
	public static final short channel_LOGIN = 23113;//从渠道登录
	
	//pve章节协议
	public static final short PVE_PAGE_REQ = 23201;
	public static final short PVE_PAGE_RET = 23202;
	
	public static final short PVE_BATTLE_OVER_REPORT = 23203;
	public static final short PVE_STAR_REWARD_INFO_REQ = 23204;
	public static final short PVE_STAR_REWARD_INFO_RET = 23205;
	public static final short PVE_STAR_REWARD_GET = 23206;
	public static final short PVE_STAR_REWARD_GET_RET = 23207;
	
	public static final short PVE_GuanQia_Request = 23210;
	public static final short PVE_GuanQia_Info = 23211;
	public static final short C_PVE_Reset_CQ = 23212;
	public static final short S_PVE_Reset_CQ = 23213;
	
	public static final short C_PVE_SAO_DANG = 23220;
	public static final short S_PVE_SAO_DANG = 23222;
	
	public static final short C_YuanJun_List_Req = 23230;
	public static final short S_YuanJun_List = 23231;
	
	public static final short C_BuZhen_Report = 23240;//客户端向服务器发送布阵信息
	public static final short C_MIBAO_SELECT = 23241;//客户端向服务器发送秘宝选择信息
	public static final short S_MIBAO_SELECT_RESP = 23242;//服务器发送秘宝选择信息
	
	/** pve战斗请求 **/
	public static final short ZHANDOU_INIT_PVE_REQ = 24201;
	/** pvp战斗请求 **/
	public static final short ZHANDOU_INIT_PVP_REQ = 24202;
	/** 掠夺战斗请求**/
	public static final short ZHANDOU_INIT_LVE_DUO_REQ = 24203;
	
	/** pve、pvp、押镖、劫镖、掠夺、荒野战斗请求返回数据 **/
	public static final short ZHANDOU_INIT_RESP = 24151;
	
	//通关奖励
	public static final short BattlePveResult_Req = 23300;
	public static final short Award_Item = 23301;
	public static final short BattlePve_Result = 23302;
	
	public static final short C_Report_battle_replay = 23310;
	public static final short C_Request_battle_replay = 23311;
	
	public static final short PLAYER_STATE_REPORT = 23401;
	
	public static final short C_APP_SLEEP = 23411;//客户端报告进入后台
	public static final short C_APP_WAKEUP = 23413;//客户端报告程序进入前台
	
	public static final short PLAYER_SOUND_REPORT = 23501;
	public static final short C_get_sound = 23505;
	public static final short S_get_sound = 23507;
	
	public static final short JunZhuInfoReq = 23601;
	public static final short JunZhuInfoRet = 23602;
	public static final short JunZhuAttPointReq = 23603;
	public static final short JunZhuAttPointRet = 23604;
	public static final short JunZhuAddPointReq = 23605;
	
	public static final short C_KeJiInfo = 23650;
	public static final short S_KeJiInfo = 23651;
	public static final short C_KeJiUp = 23652;
	
	public static final short C_EquipAdd = 23701;
	public static final short C_EquipRemove = 23702;
	public static final short S_EquipInfo = 23703;
	public static final short S_BagInfo = 23704;
	public static final short C_BagInfo = 23705;
	public static final short C_EquipInfo = 23706;//装备列表
	
	public static final short C_EquipDetailReq = 23710;
	public static final short S_EquipDetail = 23711;
	
	public static final short C_EquipInfoOtherReq = 23712;// 别人装备详情
	public static final short S_EquipInfoOther = 23713;
	
	////Equip
	//装备列表
	public static final short C_EQUIP_LIST = 24001;
	public static final short S_EQUIP_LIST = 24002;
	//装备强化
	public static final short C_EQUIP_UPGRADE = 24003;
	public static final short S_EQUIP_UPGRADE = 24004;
	public static final short C_EQUIP_XiLian = 24012;
	public static final short S_EQUIP_XiLian = 24013;
	public static final short C_EQUIP_JINJIE = 24015;
	public static final short S_EQUIP_JINJIE = 24016;
	public static final short S_EQUIP_XILIAN_ERROR = 24018;
	public static final short C_EQUIP_UPALLGRADE=24019;//一键强化
	public static final short S_EQUIP_UPALLGRADE=24020;//一键强化返回
	
	public static final short C_JingMai_info = 24100;
	public static final short C_JingMai_up = 24101;
	public static final short S_JingMai_info = 24103;
	
	//mail sys protocol code
	public static final short C_REQ_MAIL_LIST = 25003;
	public static final short S_REQ_MAIL_LIST = 25004;
//	public static final short C_DELETE_MAIL = 25005;
	public static final short S_DELETE_MAIL = 25006;
	public static final short C_MAIL_GET_REWARD = 25007;
	public static final short S_MAIL_GET_REWARD = 25008;
	public static final short S_MAIL_NEW = 25010;
	public static final short C_SEND_EAMIL = 25011;
	public static final short S_SEND_EAMIL = 25012;
	public static final short C_READED_EAMIL = 25013;
	public static final short S_READED_EAMIL = 25014;
	public static final short C_EMAIL_RESPONSE = 25015;
	public static final short S_EMAIL_RESPONSE = 25016;
	
	//武将
	public static final short HERO_INFO_REQ 			= 26001;
	public static final short HERO_DATA 				= 26002;
	public static final short HERO_INFO 				= 26003;
	public static final short WUJIANG_TECHINFO_REQ  	= 26004;
	public static final short WUJIANG_TECHLEVELUP_REQ   = 26005;
	public static final short WUJIANG_LEVELUP_REQ		= 26006;
	public static final short HERO_ACTIVE_REQ = 26007;
	public static final short JINGPO_REFRESH_REQ = 26008;			//已废弃
	public static final short WUJIANG_TECH_SPEEDUP_REQ = 26009;

	
	public static final short WUJIANG_TECHINFO_RESP 	= 26054;
	public static final short WUJIANG_TECHLEVELUP_RESP  = 26055;
	public static final short WUJIANG_LEVELUP_RESP 		= 26056;
	public static final short HERO_ACTIVE_RESP = 26057;
	public static final short JINGPO_REFRESH_RESP = 26058;
	public static final short WUJIANG_TECH_SPEEDUP_RESP= 26059;
	
	// 掠夺协议
	public static final short LVE_DUO_INFO_REQ = 26060;
	public static final short LVE_DUO_INFO_RESP = 26061;
	public static final short LVE_CONFIRM_REQ = 26062;
	public static final short LVE_CONFIRM_RESP = 26063;
	public static final short LVE_BATTLE_RECORD_REQ = 26064;
	public static final short LVE_BATTLE_RECORD_RESP = 26065;
	public static final short LVE_GO_LVE_DUO_REQ = 26066;
	public static final short LVE_GO_LVE_DUO_RESP = 26067;
	public static final short LVE_BATTLE_END_REQ = 26068;
	public static final short LVE_NEXT_ITEM_REQ = 26069;
	public static final short LVE_NEXT_ITEM_RESP = 26070;
	public static final short LVE_BATTLE_END_RESP = 26071;
	public static final short LVE_NOTICE_CAN_LVE_DUO = 26072;
	public static final short LVE_HELP_REQ = 26073;
	public static final short LVE_HELP_RESP = 26074;
	// 掠夺战斗请求协议是：24203（和pve，pvp写在一起定义）;
	// 掠夺战斗请求返回协议时：24151
	
	// 百战千军协议类型
	/**请求百战 **/
	public static final short BAIZHAN_INFO_REQ		= 27001;
	/** **/
	public static final short BAIZHAN_INFO_RESP		= 27002;
	/** 请求 挑战 **/
	public static final short CHALLENGE_REQ 		= 27011;
	/** **/
	public static final short CHALLENGE_RESP 		= 27012;
	/**请求 百战千军中确定做某种事情**/
	public static final short CONFIRM_EXECUTE_REQ	= 27015;
	/** **/
	public static final short CONFIRM_EXECUTE_RESP	= 27016;
	/** 前台发送百战千军的结果**/
	public static final short BAIZHAN_RESULT	    = 27017;
	/** 挑战者状态 **/
	public static final short PLAYER_STATE_REQ	    = 27018;
	public static final short PLAYER_STATE_RESP	    = 27019;
	/** 战斗记录请求 **/
	public static final short ZHAN_DOU_RECORD_REQ      = 27022;
	/** 战斗记录响应 **/
	public static final short ZHAN_DOU_RECORD_RESP      = 27023;
	/**是27017的响应页面发送**/
	public static final short BAIZHAN_RESULT_RESP    =  27024;
	
	// 刷新挑战对手列表请求
	public static final short REFRESH_ENEMY_LIST_REQ	    = 27026;
	public static final short REFRESH_ENEMY_LIST_RESP = 27027;
	
	
	public static final short C_LM_HOUSE_INFO = 27301;
	public static final short S_LM_HOUSE_INFO = 27302;
	public static final short S_LM_UPHOUSE_INFO = 27303;//更新房屋信息
	public static final short C_HOUSE_EXCHANGE_RQUEST = 27311;
	public static final short S_HOUSE_EXCHANGE_RESULT = 27312;
	public static final short C_HOUSE_APPLY_LIST = 27313;
	public static final short S_HOUSE_APPLY_LIST = 27314;
	public static final short C_AnswerExchange = 27321;
	
	public static final short C_Set_House_state = 27303;
	public static final short C_EnterOrExitHouse = 27305;
	public static final short C_GetHouseVInfo = 27306;//请求访客列表
	public static final short C_get_house_exp = 27307;//获取小房子经验
	public static final short S_house_exp = 27308;
	public static final short C_huan_wu_info = 27309;
	public static final short S_huan_wu_info = 27310;
	public static final short C_huan_wu_Oper = 27331;
	public static final short C_huan_wu_list = 27333;
	public static final short S_huan_wu_list = 27334;
	public static final short C_huan_wu_exchange = 27337;
	public static final short S_huan_wu_exchange = 27338;
	public static final short C_ExCanJuanJiangLi = 27341;
	public static final short S_ExCanJuanJiangLi = 27342;
	public static final short C_up_house = 27343;
	public static final short C_Pai_big_house = 27351;
	public static final short S_Pai_big_house = 27352;
	public static final short C_GET_BIGHOUSE_EXP = 27353;//获取大房子经验
	public static final short S_HouseVInfo = 27354;//发送访客列表
	public static final short C_ShotOffVisitor = 27355;//踢出访客
	public static final short S_ShotOffVisitor = 27356;//访客被踢
	public static final short C_EHOUSE_EXCHANGE_RQUEST =  27357;//请求交换空房屋
	public static final short S_EHOUSE_EXCHANGE_RESULT =  27358;//请求交换空房屋返回结果
	public static final short C_CHANGE_BIGHOUSE_WORTH =  27359;//请求衰减高级房屋价值
	public static final short C_CANCEL_EXCHANGE =  27360;//请求撤回交换房屋申请
	public static final short S_CANCEL_EXCHANGE =  27361;//请求撤回交换房屋申请返回结果
	public static final short C_get_house_info = 27362;//获取自己房子信息
	public static final short S_house_info = 27363;//推送房子信息
	//
	public static final short C_JIAN_ZHU_INFO = 27401;
	public static final short S_JIAN_ZHU_INFO = 27402;
	public static final short C_JIAN_ZHU_UP = 27403;
	public static final short S_JIAN_ZHU_UP = 27404;
	//
	//联盟 28001~28099(预计)
	public static final short GET_UNION_INFO_REQ         = 28001;
	public static final short UNION_APPLY_JION_REQ       = 28002;
	public static final short GET_UNION_FRIEND_INFO_REQ  = 28003;
	public static final short UNION_EDIT_REQ             = 28004;
	public static final short UNION_INNER_EDIT_REQ 	 	 = 28005;
	public static final short UNION_OUTER_EDIT_REQ 	 	 = 28006;
	public static final short CREATE_UNION_REQ 		     = 28007;
	public static final short UNION_LEVELUP_REQ 		 = 28008;
	public static final short UNION_APPLY_REQ 			 = 28009;
	public static final short UNION_INVITE_REQ 		 	 = 28010;
	public static final short UNION_INVITED_AGREE_REQ 	 = 28011;
	public static final short UNION_INVITED_REFUSE_REQ   = 28012;
	public static final short UNION_QUIT_REQ 			 = 28013;
	public static final short UNION_DISMISS_REQ 		 = 28014;
	public static final short UNION_TRANSFER_REQ 		 = 28015;
	public static final short UNION_ADVANCE_REQ 		 = 28016;
	public static final short UNION_DEMOTION_REQ 		 = 28017;
	public static final short UNION_REMOVE_REQ 		 	 = 28018;
	public static final short UNION_DETAIL_INFO_REQ 	 = 28019;
	       
	public static final short GET_UNION_INFO_RESP 		 = 28051;
	public static final short UNION_APPLY_JION_RESP      = 28052;
	public static final short GET_UNION_FRIEND_RESP 	 = 28053;
	public static final short UNION_EDIT_RESP 			 = 28054;
	public static final short UNION_INNER_EDIT_RESP 	 = 28055;
	public static final short UNION_OUTER_EDIT_RESP 	 = 28056;
	public static final short CREATE_UNION_RESP 		 = 28057;
	public static final short UNION_LEVELUP_RESP 		 = 28058;
	public static final short UNION_APPLY_RESP 		 	 = 28059;
	public static final short UNION_INVITE_RESP 		 = 28060;
	public static final short UNION_INVITED_AGREE_RESP   = 28061;
	public static final short UNION_INVITED_REFUSE_RESP  = 28062;
	public static final short UNION_QUIT_RESP 			 = 28063;
	public static final short UNION_DISMISS_RESP 		 = 28064;
	public static final short UNION_TRANSFER_RESP 		 = 28065;
	public static final short UNION_ADVANCE_RESP 		 = 28066;
	public static final short UNION_DEMOTION_RESP 		 = 28067;
	public static final short UNION_REMOVE_RESP 		 = 28068;
	public static final short UNION_DETAIL_INFO 		 = 28069;
	
	public static final short C_get_daily_award_info	 = 28100;
	public static final short S_daily_award_info		 = 28110;
	public static final short C_get_daily_award			 = 28120;
	
	// ************  定时请求操作指令 	*************
	/** 玩家定时请求任务 **/
	public static final short C_TIMEWORKER_INTERVAL = 28301;
	/** 发送玩家定时请求结果 **/
	public static final short S_TIMEWORKER_INTERVAL = 28302;
	
	//**************  商城指令 	*******************
	//抽卡预留28201~28299
	public static final short BUY_CARDBAG_REQ			 = 28201;
	public static final short OPEN_CARDBAG_REQ			 = 28202;
	
	public static final short BUY_CARDBAG_RESP			 = 28251;
	public static final short OPEN_CARDBAG_RESP			 = 28252;
	
	/** 请求宝箱购买信息 **/
	public static final short BUY_TREASURE_INFOS_REQ = 28253;
	/** 返回宝箱购买信息 **/
	public static final short BUY_TREASURE_INFOS_RESP = 28254;
	/** 购买宝箱 **/
	public static final short BUY_TREASURE = 28255;
	/** 返回购买宝箱获得物品信息 **/
	public static final short BUY_TREASURE_RESP = 28256;
	/** 请求资源购买信息 **/
	public static final short BUY_RESOURCE_INFOS_REQ = 28257;
	/** 返回资源购买信息 **/
	public static final short BUY_RESOURCE_INFOS_RESP = 28258;
	/** 返回商城购买失败信息 **/
	public static final short PURCHASE_FAIL = 28260;
	
	
	/** 请求购买体力和铜币的次数 **/
	public static final short C_BUY_TIMES_REQ = 28321;
	/** 返回购买体力个铜币的次数 **/
	public static final short S_BUY_TIMES_INFO = 28322;
	/** 购买体力 **/
	public static final short C_BUY_TiLi = 28323;
	/** 购买铜币 **/
	public static final short C_BUY_TongBi = 28324;
	/** 购买铜币返回 **/
	public static final short S_BUY_TongBi = 28325;
	/** 购买秘宝升级点数 **/
	public static final short C_BUY_MIBAO_POINT = 28327;
	/** 购买秘宝升级点数返回 **/
	public static final short S_BUY_MIBAO_POINT_RESP = 28328;
	
	//**************  成就指令  ***************
	/** 请求成就列表 **/
	public static final short C_ACHE_LIST_REQ = 28331;
	/** 返回成就列表 **/
	public static final short S_ACHE_LIST_RESP = 28332;
	/** 成就完成通知 **/
	public static final short S_ACHE_FINISH_INFORM = 28334;
	/** 领取成就奖励 **/
	public static final short C_ACHE_GET_REWARD_REQ = 28335;
	/** 领取成就奖励返回结果 **/
	public static final short S_ACHE_GET_REWARD_RESP = 28336;
	
	//***************  每日任务指令  *******************
	/** 请求每日任务列表 **/
	public static final short C_DAILY_TASK_LIST_REQ = 28341;
	/** 返回每日任务列表 **/
	public static final short S_DAILY_TASK_LIST_RESP = 28342;
	/** 每日任务完成通知 **/
	public static final short S_DAILY_TASK_FINISH_INFORM = 28344;
	/** 领取每日任务奖励 **/
	public static final short C_DAILY_TASK_GET_REWARD_REQ = 28345;
	/** 领取每日任务奖励返回结果 **/
	public static final short S_DAILY_TASK_GET_REWARD_RESP = 28346;

	
	public static final short C_BuZhen_Hero_Req = 29401;
	public static final short S_BuZhen_Hero_Info = 29402;

	public static final short C_TaskReq = 29501;
	public static final short S_TaskList = 29502;
	public static final short S_TaskSync = 29503;//
	public static final short C_GetTaskReward = 29504;//
	public static final short S_GetTaskRwardResult = 29505;//
	public static final short C_TaskProgress = 29506;//客户端汇报任务进度
	
	public static final short C_YuJueHeChengRequest = 29509;
	public static final short S_YuJueHeChengResult = 29510;
	public static final short S_NEW_CHENGHAO = 29520;
	
	//秘宝协议
	/** 秘宝激活请求 **/
	public static final short C_MIBAO_ACTIVATE_REQ = 29601;
	/** 秘宝激活结果返回 **/
	public static final short S_MIBAO_ACTIVATE_RESP = 29602;
	/** 秘宝信息请求 **/
	public static final short C_MIBAO_INFO_REQ = 29603;
	/** 秘宝信息返回 **/
	public static final short S_MIBAO_INFO_RESP = 29604;
	/** 秘宝升级请求 **/
	public static final short C_MIBAO_LEVELUP_REQ = 29605;
	/** 秘宝升级结果返回 **/
	public static final short S_MIBAO_LEVELUP_RESP = 29606;
	/** 秘宝升星请求 **/
	public static final short C_MIBAO_STARUP_REQ = 29607;
	/** 秘宝升星结果返回 **/
	public static final short S_MIBAO_STARUP_RESP = 29608;
	/** 别人秘宝信息请求 **/
	public static final short C_MIBAO_INFO_OTHER_REQ = 29609;
	/** 别人秘宝信息返回 **/
	public static final short S_MIBAO_INFO_OTHER_RESP = 29610;
	/** 手动激活（进阶）秘宝技能**/
	public static final short MIBAO_DEAL_SKILL_REQ = 29611;
	public static final short MIBAO_DEAL_SKILL_RESP = 29612;
	/**领奖（因为秘宝总星星数达到要求）*/
	public static final short GET_FULL_STAR_AWARD_REQ = 29613;
	public static final short GET_FULL_STAR_AWARD_RESP = 29614;
	
	//***************  探宝协议  *******************
	/**请求矿区主界面**/
	public static final short EXPLORE_INFO_REQ    = 30002;
	/**响应矿区主界面**/
	public static final short EXPLORE_INFO_RESP   = 30003;
	/**请求采矿**/
	public static final short EXPLORE_REQ         = 30004;
	/**响应不可以采矿**/
	public static final short EXPLORE_RESP        = 30005;
	/**响应发送采矿奖励信息**/
	public static final short EXPLORE_AWARDS_INFO = 30006;
	
	
	//当铺
	/** 卖出物品 **/
	public static final short PAWN_SHOP_GOODS_SELL = 30021;
	/** 卖出物品成功 **/
	public static final short PAWN_SHOP_GOODS_SELL_OK = 30022;
	/** 请求当铺物品列表 **/
	public static final short PAWN_SHOP_GOODS_LIST_REQ = 30023;
	/** 返回当铺物品列表 **/
	public static final short PAWN_SHOP_GOODS_LIST = 30024;
	/** 购买物品 **/
	public static final short PAWN_SHOP_GOODS_BUY = 30025;
	/** 购买物品成功 **/
	public static final short PAWN_SHOP_GOODS_BUY_RESP = 30026;
	/** 手动刷新当铺物品 **/
	public static final short PAWN_SHOP_GOODS_REFRESH = 30027;
	/** 手动刷新当铺物品 **/
	public static final short PAWN_SHOP_GOODS_REFRESH_RESP = 30028;
	
	//***************** 联盟协议  ******************
	/** 从npc处点击查看联盟 **/
	public static final short ALLIANCE_INFO_REQ = 30100;
	/** 返回联盟信息， 给没有联盟的玩家返回此条信息 **/
	public static final short ALLIANCE_NON_RESP = 30101;
	/** 返回联盟信息， 给有联盟的玩家返回此条信息 **/
	public static final short ALLIANCE_HAVE_RESP = 30102;
	/** 验证联盟名字 **/
	public static final short CHECK_ALLIANCE_NAME = 30103;
	/** 返回验证联盟结果 **/
	public static final short CHECK_ALLIANCE_NAME_RESP = 30104;
	/** 创建联盟 **/
	public static final short CREATE_ALLIANCE = 30105;
	/** 返回创建联盟结果 **/
	public static final short CREATE_ALLIANCE_RESP = 30106;
	/** 查找联盟 **/
	public static final short FIND_ALLIANCE = 30107;
	/** 返回查找联盟结果 **/
	public static final short FIND_ALLIANCE_RESP = 30108;
	/** 申请联盟 **/
	public static final short APPLY_ALLIANCE = 30109;
	/** 返回申请联盟结果 **/
	public static final short APPLY_ALLIANCE_RESP = 30110;
	/** 取消加入联盟 **/
	public static final short CANCEL_JOIN_ALLIANCE = 30111;
	/** 返回取消加入联盟结果 **/
	public static final short CANCEL_JOIN_ALLIANCE_RESP = 30112;
	/** 退出联盟 **/
	public static final short EXIT_ALLIANCE = 30113;
	/** 退出联盟成功 **/
	public static final short EXIT_ALLIANCE_RESP = 30114;
	/** 查看联盟成员 **/
	public static final short LOOK_MEMBERS = 30115;
	/** 返回联盟成员信息 **/
	public static final short LOOK_MEMBERS_RESP = 30116;
	/** 开除成员**/
	public static final short FIRE_MEMBER = 30117;
	/** 开除成员返回**/
	public static final short FIRE_MEMBER_RESP = 30118;
	/** 升职成员**/
	public static final short UP_TITLE = 30119;
	/** 升职成员返回**/
	public static final short UP_TITLE_RESP = 30120;
	/** 降职成员**/
	public static final short DOWN_TITLE = 30121;
	/** 降职成员返回**/
	public static final short DOWN_TITLE_RESP = 30122;
	/** 查看申请联盟玩家**/
	public static final short LOOK_APPLICANTS = 30123;
	/** 查看申请联盟玩家结果返回**/
	public static final short LOOK_APPLICANTS_RESP = 30124;
	/** 拒绝申请**/
	public static final short REFUSE_APPLY = 30125;
	/** 拒绝申请返回**/
	public static final short REFUSE_APPLY_RESP = 30126;
	/** 同意申请**/
	public static final short AGREE_APPLY = 30127;
	/** 同意申请返回**/
	public static final short AGREE_APPLY_RESP = 30128;
	/** 修改公告**/
	public static final short UPDATE_NOTICE = 30129;
	/** 修改公告返回**/
	public static final short UPDATE_NOTICE_RESP = 30130;
	/** 解散联盟**/
	public static final short DISMISS_ALLIANCE = 30131;
	/** 解散联盟返回**/
	public static final short DISMISS_ALLIANCE_OK = 30132;
	/** 打开招募**/
	public static final short OPEN_APPLY = 30133;
	/** 打开招募返回**/
	public static final short OPEN_APPLY_RESP = 30134;
	/** 关闭招募**/
	public static final short CLOSE_APPLY = 30135;
	/** 关闭招募返回成功**/
	public static final short CLOSE_APPLY_OK = 30136;
	/** 转让联盟**/
	public static final short TRANSFER_ALLIANCE = 30137;
	/** 转让联盟返回**/
	public static final short TRANSFER_ALLIANCE_RESP = 30138;
	/** 盟主选举报名**/
	public static final short MENGZHU_APPLY = 30139;
	/** 盟主选举报名结果返回**/
	public static final short MENGZHU_APPLY_RESP = 30140;
	/** 盟主选举报名**/
	public static final short MENGZHU_VOTE = 30141;
	/** 盟主选举报名结果返回**/
	public static final short MENGZHU_VOTE_RESP = 30142;
	/** 放弃投票 **/
	public static final short GIVEUP_VOTE = 30143;
	/** 放弃投票结果返回 **/
	public static final short GIVEUP_VOTE_RESP = 30144;
	/** 立刻加入联盟 **/
	public static final short IMMEDIATELY_JOIN = 30145;
	/** 立刻加入联盟返回 **/
	public static final short IMMEDIATELY_JOIN_RESP = 30146;
	/** 加入联盟被批准通知 **/
	public static final short ALLIANCE_ALLOW_NOTIFY = 30147;
	/** 被联盟开除通知 **/
	public static final short ALLIANCE_FIRE_NOTIFY = 30148;
	/** 联盟虎符捐献 **/
	public static final short ALLIANCE_HUFU_DONATE = 30149;
	public static final short ALLIANCE_HUFU_DONATE_RESP = 30150;
	/** 有新的申请成员通知 **/
	public static final short ALLIANCE_HAVE_NEW_APPLYER = 30160;
	/** 联盟事件请求 */
	public static final short ALLINACE_EVENT_REQ = 30161;
	/** 联盟事件返回 */
	public static final short ALLINACE_EVENT_RESP = 30162;
	/** 联盟升级通知 */
	public static final short ALLIANCE_LEVEL_UP_NOTIFY = 30164;
	/** 联盟解散通知 **/
	public static final short ALLIANCE_DISMISS_NOTIFY = 30166;
	
	public static final short C_SETTINGS_GET = 30201;//客户端获取设置
	public static final short C_SETTINGS_SAVE = 30203;//客户端请求保存设置
	public static final short S_SETTINGS = 30204;//服务器发给客户端设置
	public static final short C_change_name = 30301;
	public static final short S_change_name = 30302;
	
	
	public static final short C_MoBai_Info = 4010;
	public static final short S_MoBai_Info = 4011;
	public static final short C_MoBai = 4012;
	public static final short C_GET_MOBAI_AWARD = 4022;
	/** 加入聊天黑名单 **/
	public static final short C_JOIN_BLACKLIST = 30151;
	/** 加入聊天黑名单返回 **/
	public static final short S_JOIN_BLACKLIST_RESP = 30152;
	/** 查看黑名单 **/
	public static final short C_GET_BALCKLIST = 30153;
	/** 返回黑名单列表 **/
	public static final short S_GET_BALCKLIST = 30154;
	/** 取消屏蔽 **/
	public static final short C_CANCEL_BALCK = 30155;
	/** 取消屏蔽结果 **/
	public static final short S_CANCEL_BALCK = 30156;
	
	
	//***************** 荒野求生协议  ******************
	public static final short HY_SHOP_REQ = 30390;
	public static final short HY_SHOP_RESP = 30391;
	public static final short HY_BUY_GOOD_REQ = 30392;
	public static final short HY_BUY_GOOD_RESP = 30393;
	public static final short ACTIVE_TREASURE_REQ = 30394;
	public static final short ACTIVE_TREASURE_RESP = 30395;
	public static final short MAX_DAMAGE_RANK_REQ = 30396;
	public static final short MAX_DAMAGE_RANK_RESP = 30397;
	public static final short HY_BUY_BATTLE_TIMES_REQ = 30398;
	public static final short HY_BUY_BATTLE_TIMES_RESP = 30399;
	/** 打开荒野 **/
	public static final short C_OPEN_HUANGYE = 30401;
	public static final short S_OPEN_HUANGYE = 30402;
//	/** 驱散迷雾 **/
//	public static final short C_OPEN_FOG = 30403;
//	public static final short S_OPEN_FOG = 30404;
	/** 开启藏宝点 **/
	public static final short C_OPEN_TREASURE = 30405;
	public static final short S_OPEN_TREASURE = 30406;
//	/** 请求奖励库 **/
//	public static final short C_REQ_REWARD_STORE = 30407;
//	public static final short S_REQ_REWARD_STORE = 30408;
//
//	/** 申请奖励 **/
//	public static final short C_APPLY_REWARD = 30409;
//	public static final short S_APPLY_REWARD = 30410;
//	/** 取消申请奖励 **/
//	public static final short C_CANCEL_APPLY_REWARD = 30411;
//	public static final short S_CANCEL_APPLY_REWARD = 30412;
//	/** 盟主分配奖励 **/
//	public static final short C_GIVE_REWARD = 30413;
//	public static final short S_GIVE_REWARD = 30414;
	/** 荒野pve-藏宝点挑战 **/
	public static final short C_HUANGYE_PVE = 30415;
	public static final short S_HUANGYE_PVE_RESP = 30416;
	/** 荒野pve-查看藏宝点信息 **/
	public static final short C_HYTREASURE_BATTLE = 30417;
	public static final short S_HYTREASURE_BATTLE_RESP = 30418;
	/** 荒野pve-藏宝点战斗结束 **/
	public static final short C_HUANGYE_PVE_OVER = 30419;
	public static final short S_HUANGYE_PVE_OVER_RESP = 30420;
//	/** 荒野pvp-资源点挑战 **/
//	public static final short C_HUANGYE_PVP = 30421;
//	public static final short S_HUANGYE_PVP_RESP = 30422;
//	/** 荒野pvp-资源点战斗结束 **/
//	public static final short C_HUANGYE_PVP_OVER = 30423;
//	public static final short S_HUANGYE_PVP_OVER_RESP = 30424;
//	/** 荒野pvp-查看资源点信息 **/
//	public static final short C_HYRESOURCE_BATTLE = 30425;
//	public static final short S_HYRESOURCE_BATTLE_RESP = 30426;
//	/** 更换资源点 **/
//	public static final short C_HYRESOURCE_CHANGE = 30427;
//	public static final short S_HYRESOURCE_CHANGE_RESP = 30428;
	
	
	/**排行榜**/
	public static final short RANKING_REP = 30430;
	public static final short RANKING_RESP = 30431;
	
	/*
	 * 充值
	 */
	/**请求充值**/
	public static final short C_RECHARGE_REQ = 30432;
	public static final short S_RECHARGE_RESP = 30433;
	/**请求充值页面(vip信息)**/
	public static final short C_VIPINFO_REQ = 30434;
	public static final short S_VIPINFO_RESP = 30435;
	// 获取包含了pve秘宝的战力
	public static final short C_PVE_MIBAO_ZHANLI = 30436;
	public static final short S_PVE_MIBAO_ZHANLI = 30437;
	
	/*
	 * 天赋
	 */
	public static final short TALENT_INFO_REQ = 30537;
	public static final short TALENT_INFO_RESP = 30538;
	public static final short TALENT_UP_LEVEL_REQ = 30539;
	public static final short TALENT_UP_LEVEL_RESP = 30540;
	// 通知玩家天赋可以升级
	public static final short NOTICE_TALENT_CAN_UP = 30541;
	// 通知玩家天赋不可以升级
	public static final short NOTICE_TALENT_CAN_NOT_UP = 30542;
	
	//好友协议
	/**获取好友列表**/
	public static final short C_FRIEND_REQ = 31001;
	public static final short S_FRIEND_RESP = 31002;
	/**请求添加好友**/
	public static final short C_FRIEND_ADD_REQ = 31003;
	public static final short S_FRIEND_ADD_RESP = 31004;
	/**请求删除好友**/
	public static final short C_FRIEND_REMOVE_REQ = 31005;
	public static final short S_FRIEND_REMOVE_RESP = 31006;
	
	public static final short C_GET_FRIEND_IDS = 31011;
	public static final short S_GET_FRIEND_IDS = 31012;
	
	// 活动协议
	/**请求签到**/
	public static final short C_QIANDAO_REQ = 32001; 
	public static final short S_QIANDAO_RESP = 32002; 
	/**请求签到情况**/
	public static final short C_GET_QIANDAO_REQ = 32003; 
	public static final short S_GET_QIANDAO_RESP = 32004; 
	/**获取所有的活动列表**/
	public static final short C_GET_ACTIVITYLIST_REQ = 32101;
	public static final short S_GET_ACTIVITYLIST_RESP = 32102;
	/**获取首冲详情**/
	public static final short C_GET_SHOUCHONG_REQ = 32201;
	public static final short S_GET_SHOUCHONG_RESP = 32202;
	/**领取首冲奖励**/
	public static final short C_SHOUCHONG_AWARD_REQ = 32203;
	public static final short S_SHOUCHONG_AWARD_RESP = 32204;
	/**转国**/
	public static final short C_ZHUANGGUO_REQ = 32205;
	public static final short S_ZHUANGGUO_RESP = 32206;
	
	/**押镖**/
	public static final short C_YABIAO_INFO_REQ	= 3401;//请求押镖活动界面
	public static final short S_YABIAO_INFO_RESP= 3402;//请求押镖活动界面返回
	public static final short C_YABIAO_MENU_REQ = 3403;//请求选马界面
	public static final short S_YABIAO_MENU_RESP = 3404;//请求选马界面返回
	public static final short C_SETHORSE_REQ = 3405;//请求设置马匹
	public static final short S_SETHORSE_RESP = 3406;//请求设置马匹返回
	public static final short C_YABIAO_REQ = 3407;//请求开始押镖
	public static final short S_YABIAO_RESP = 3408;//请求开始押镖返回
//	public static final short C_JIEBIAO_INFO_REQ= 3409;//请求劫镖界面
//	public static final short S_JIEBIAO_INFO_RESP= 3410;//请求劫镖界面返回
	public static final short C_ENTER_YABIAOSCENE = 3411;//请求进入押镖场景
	public static final short C_ENTER_JBBATTLE_REQ = 3412;//请求劫镖返回
	public static final short S_BIAOCHE_INFO_RESP = 3413;//推送镖车信息
	public static final short C_ENDYABIAO_REQ = 3414;//测试用
	public static final short S_ENDYABIAO_RESP = 3415;//测试用
	public static final short S_BIAOCHE_MOVE = 3416;//推送镖车移动	
	public static final short S_BIAOCHE_STATE = 3417;//推送镖车战斗状态
	public static final short C_BIAOCHE_INFO = 3418;//请求镖车信息
	public static final short C_ZHANDOU_INIT_YB_REQ = 3419;//请求战斗配置
	public static final short C_YABIAO_RESULT = 3420;//请求战斗结算
	public static final short S_ZHANDOU_INIT_ERROR = 3421;//请求战斗错误返回
	public static final short S_YABIAO_ENTER_RESP = 3422;//有新的押镖者进入场景
	public static final short C_YABIAO_ENEMY_RSQ = 3423;//请求押镖仇人
	public static final short S_YABIAO_ENEMY_RESP = 3424;//请求押镖仇人返回
	public static final short C_YABIAO_BUY_RSQ = 3425;//请求够买押镖相关次数
	public static final short S_YABIAO_BUY_RESP = 3426;//请求够买押镖相关次数返回
	public static final short C_YABIAO_HISTORY_RSQ = 3427;//请求押镖历史
	public static final short S_YABIAO_HISTORY_RESP = 3428;//请求押镖历史返回
	public static final short C_YABIAO_HELP_RSQ = 3429;//请求押镖协助
	public static final short S_YABIAO_HELP_RESP = 3430;//请求押镖协助返回
	public static final short C_ANSWER_YBHELP_RSQ = 3431;//答复押镖协助
	public static final short S_ANSWER_YBHELP_RESP = 3432;//答复押镖协助返回
	public static final short C_TICHU_YBHELP_RSQ = 3433;//踢出押镖协助
	public static final short S_TICHU_YBHELP_RESP = 3434;//踢出押镖协助返回
	public static final short S_ASK_YABIAO_HELP_RESP = 3435;//答复请求押镖协助返回 
//	public static final short C_YABIAO_XIEZHU_TIMES_RSQ = 3436;//请求押镖协助次数
//	public static final short S_YABIAO_XIEZHU_TIMES_RESP = 3437;//请求押镖协助次数返回 
	public static final short S_TICHU_YBHELPXZ_RESP = 3438;//踢出押镖协助者给协助者返回
	public static final short S_PUSH_YBRECORD_RESP = 3439;//推送押镖战斗记录
	public static final short C_BUYHORSEBUFF_REQ = 3440;//请求购买马车buff
	public static final short S_BUYHORSEBUFF_RESP = 3441;//请求购买马车buff返回
//	public static final short C_MOVE2BIAOCHE_REQ = 3442;//请求镖车坐标
//	public static final short S_MOVE2BIAOCHE_RESP = 3443;//请求镖车坐标返回
	public static final short C_CARTJIASU_REQ = 3444;//请求镖车加速
	public static final short S_CARTJIASU_RESP = 3445;//请求镖车加速返回
	public static final short C_YABIAO_XIEZHUS_REQ = 3446;//请求协助君主列表
	public static final short S_YABIAO_XIEZHUS_RESP = 3447;//请求协助君主列表返回
	
	/*========== 游侠战斗 ================*/ 
	/**
	 * 游侠战斗请求
	 */
	public static final short C_YOUXIA_INIT_REQ = 601;
	
	/**
	 * 游侠战斗请求返回
	 */
	public static final short S_YOUXIA_INIT_RESP = 602;
	
	/**
	 * 游侠战斗结果
	 */
	public static final short C_YOUXIA_BATTLE_OVER_REQ = 603;
	
	/**
	 * 游侠战斗结果返回
	 */
	public static final short S_YOUXIA_BATTLE_OVER_RESP = 604;
	
	/**
	 * 游戏玩法信息请求
	 */
	public static final short C_YOUXIA_INFO_REQ = 605;
	
	/**
	 * 游戏玩法信息请求返回
	 */
	public static final short S_YOUXIA_INFO_RESP = 606;
	
	public static final short C_YOUXIA_TIMES_INFO_REQ = 607;
	
	public static final short S_YOUXIA_TIMES_INFO_RESP = 608;
	
	public static final short C_YOUXIA_TIMES_BUY_REQ = 609;
	
	public static final short S_YOUXIA_TIMES_BUY_RESP = 610;
	
	public static final short C_YOUXIA_SAO_DANG_REQ = 611;

	public static final short S_YOUXIA_SAO_DANG_RESP = 612;
	
	public static final short C_YOUXIA_GUANQIA_REQ = 613;
	
	public static final short S_YOUXIA_GUANQIA_RESP = 614;
	
	/**限时活动**/
	public static final short C_XINSHOU_XIANSHI_INFO_REQ = 4001;//请求新手限时活动界面
	public static final short S_XINSHOU_XIANSHI_INFO_RESP = 4002;//请求新手限时活动界面返回
	public static final short C_XINSHOU_XIANSHI_AWARD_REQ = 4003;//请求领取新手限时活动奖励
	public static final short S_XINSHOU_XIANSHI_AWARD_RESP = 4004;//请求领取新手限时活动奖励返回
	public static final short C_XIANSHI_INFO_REQ = 4005;//请求限时活动界面
	public static final short S_XIANSHI_INFO_RESP = 4006;//请求限时活动界面返回
	public static final short C_XIANSHI_AWARD_REQ = 4007;//请求领取限时活动奖励
	public static final short S_XIANSHI_AWARD_RESP = 4008;//请求领取限时活动奖励返回	
	public static final short C_XIANSHI_REQ = 4009;//请求可开启的限时活动(首日/七日)
	public static final short S_XIANSHI_RESP = 4010;//请求可开启的限时活动(首日/七日)返回

	/**公告**/
	public static final short C_GET_VERSION_NOTICE_REQ = 5001;//请求版本公告
	public static final short S_GET_VERSION_NOTICE_RESP = 5002;//请求版本公告返回
	
	public static final short C_GET_CUR_CHENG_HAO = 5101;
	public static final short S_GET_CUR_CHENG_HAO = 5102;
	public static final short C_LIST_CHENG_HAO = 5111;
	public static final short S_LIST_CHENG_HAO = 5112;
	public static final short C_USE_CHENG_HAO = 5121;
	
	public static final short C_GET_UPACTION_DATA = 5131;
	public static final short S_UPACTION_DATA_0 = 5132;
	public static final short S_UPACTION_DATA_1 = 5133;
	public static final short S_UPACTION_DATA_2 = 5134;
	public static final short S_UPACTION_DATA_3 = 5135;
	
	//	捐献贡金
	public static final short C_GET_JUANXIAN_GONGJIN_REQ = 6001;//请求捐献贡金 
	public static final short S_GET_JUANXIAN_GONGJIN_RESP = 6002;//请求捐献贡金返回
	public static final short C_GET_JUANXIAN_DAYAWARD_REQ = 6007;//请求捐献贡金 日奖励
	public static final short S_GET_JUANXIAN_DAYAWARD_RESP = 6008;//请求捐献贡金日奖励返回
	// 国家主页
	public static final short GUO_JIA_MAIN_INFO_REQ = 6003;
	public static final short GUO_JIA_MAIN_INFO_RESP = 6004;
	public static final short GET_DAILY_RANK_AWARD_REQ = 6005;
	public static final short GET_DAILY_RANK_AWARD_RESP = 6006;
//	public static final short C_ISCAN_JUANXIAN_REQ = 6009;//请求是否可以捐献贡金 废弃
//	public static final short S_ISCAN_JUANXIAN_RESP = 6010;//是否可以捐献贡金返回
	
	public static final short RANKING_ALLIANCE_PLAYER_REQ=7001;// 联盟榜成员列表
	public static final short RANKING_ALLIANCE_PLAYER_RESP=7002;// 联盟榜成员列表返回
	public static final short GET_RANK_REQ=7003;// 请求名次
	public static final short GET_RANK_RESP=7004;// 请求名次返回
	
	/**符文**/
	public static final short C_QUERY_FUWEN_REQ=8001;// 请求符文主页信息
	public static final short S_QUERY_FUWEN_RESP=8002;// 返回符文主页信息
	public static final short C_OPERATE_FUWEN_REQ=8003;// 请求操作符文
	public static final short S_OPERATE_FUWEN_RESP=8004;// 返回操作符文结果
	
	/** 查看指定君主信息 */
	public static final short JUNZHU_INFO_SPECIFY_REQ = 23067;
	public static final short JUNZHU_INFO_SPECIFY_RESP = 23068;
	
	public static final short ENTER_FIGHT_SCENE = 22007;
	public static final short ENTER_FIGHT_SCENE_OK = 22008;
	public static final short FIGHT_ATTACK_REQ = 4103;
	public static final short FIGHT_ATTACK_RESP = 4104;
	/** 离开联盟战 */
	public static final short EXIT_FIGHT_SCENE = 22006;
	/** 请求联盟战信息 */
	public static final short ALLIANCE_FIGHT_INFO_REQ = 4201;
	/** 联盟站信息返回 */
	public static final short ALLIANCE_FIGHT_INFO_RESP = 4202;
	/** 联盟战报名 */
	public static final short ALLIANCE_FIGHT_APPLY = 4203;
	/** 报名结果返回 */
	public static final short ALLIANCE_FIGHT_APPLY_RESP = 4204;
	/** 请求联盟战战场信息 */
	public static final short ALLIANCE_BATTLE_FIELD_REQ = 4205;
	/** 返回联盟战战场信息 */
	public static final short ALLIANCE_BATTLE_FIELD_RESP = 4206;
	/** 联盟战有人死亡通知 */
	public static final short ALLIANCE_FIGHT_PLAYER_DEAD = 4207;
	/** 联盟战有人复活通知 */
	public static final short ALLIANCE_FIGHT_PLAYER_REVIVE = 4208;
	/** 联盟战历史战况请求 */
	public static final short ALLIANCE_FIGHT_HISTORY_REQ = 4209;
	/** 联盟战历史战况结果返回 */
	public static final short ALLIANCE_FIGHT_HISTORY_RESP = 4210;
	/** 联盟战上届排名请求 */
	public static final short ALLIANCE_FIGTH_LASTTIME_RANK = 4211;
	/** 联盟战上届排名返回 */
	public static final short ALLIANCE_FIGTH_LASTTIME_RANK_RESP = 4212;
	/** 联盟战战场信息消息推送 */
	public static final short ALLIANCE_BATTLE_FIELD_NOTIFY = 4214;
	/** 联盟战战斗结果返回*/
	public static final short ALLIANCE_BATTLE_RESULT = 4216;
	/** buffer信息*/
	public static final short BUFFER_INFO = 4217;
	/** 玩家请求复活 */
	public static final short PLAYER_REVIVE_REQUEST = 4218;
	
	

	/**红点推送通知协议号*/
	public static final short RED_NOTICE = 4220;
	public static final short FUSHI_RED_NOTICE = 4221;
	
	/**CDKey**/
	public static final short C_GET_CDKETY_AWARD_REQ = 4230;
	public static final short S_GET_CDKETY_AWARD_RESP = 4231;
	/**盟友快报*/
	public static final short C_MengYouKuaiBao_Req=4240;//请求盟友快报
	public static final short S_MengYouKuaiBao_Resq=4241;	//请求盟友快报返回
	public static final short Prompt_Action_Req = 4242; //快报中的行为请求
	public static final short Prompt_Action_Resp = 4243; //快报中行为请求返回
	/**技能培养**/
	public static final short C_GET_JINENG_PEIYANG_QUALITY_REQ=4250;
	public static final short S_GET_JINENG_PEIYANG_QUALITY_RESP=4251;
	public static final short C_UPGRADE_JINENG_REQ=4252;
	public static final short S_UPGRADE_JINENG_RESP=4253;
	
}
