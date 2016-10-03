package com.codemine.talk2me;

public class ChattingInfo {
    protected int otherLinearLayout;
    protected int ownLinearLayout;
    protected int otherHeadPortraitId;
    protected int ownHeadPortraitId;
    protected String otherDialogMsg;
    protected String ownDialogMsg;
    protected MsgType msgType;

    public ChattingInfo(int otherLinearLayout, int ownLinearLayout, int otherHeadPortraitId,
                        int ownHeadPortraitId, String otherDialogMsg, String ownDialogMsg, MsgType msgType) {
        this.otherLinearLayout = otherLinearLayout;
        this.ownLinearLayout = ownLinearLayout;
        this.otherHeadPortraitId = otherHeadPortraitId;
        this.ownHeadPortraitId = ownHeadPortraitId;
        this.otherDialogMsg = otherDialogMsg;
        this.ownDialogMsg = ownDialogMsg;
        this.msgType = msgType;
    }
}

enum MsgType {
    OTHER,
    OWN
}