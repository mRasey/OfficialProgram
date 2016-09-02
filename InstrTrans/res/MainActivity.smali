.class public Lcom/billy/dexcode/MainActivity;
.super Landroid/support/v7/app/AppCompatActivity;
.source "MainActivity.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 6
    invoke-direct {p0}, Landroid/support/v7/app/AppCompatActivity;-><init>()V

    return-void
.end method


# virtual methods
.method protected onCreate(Landroid/os/Bundle;)V
    .registers 6
    .param p1, "savedInstanceState"    # Landroid/os/Bundle;

    .prologue
    const/4 v2, 0x1

    .line 10
    invoke-super {p0, p1}, Landroid/support/v7/app/AppCompatActivity;->onCreate(Landroid/os/Bundle;)V

    .line 11
    const v1, 0x7f04001a

    invoke-virtual {p0, v1}, Lcom/billy/dexcode/MainActivity;->setContentView(I)V

    .line 13
    const-string v0, "hello"

    .line 14
    .local v0, "i":Ljava/lang/String;
    const/4 v1, -0x1

    invoke-virtual {v0}, Ljava/lang/String;->hashCode()I

    move-result v3

    sparse-switch v3, :sswitch_data_3a

    :cond_14
    :goto_14
    packed-switch v1, :pswitch_data_44

    .line 24
    :goto_17
    return-void

    .line 14
    :sswitch_18
    const-string v3, "hello"

    invoke-virtual {v0, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v3

    if-eqz v3, :cond_14

    const/4 v1, 0x0

    goto :goto_14

    :sswitch_22
    const-string v3, "world"

    invoke-virtual {v0, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v3

    if-eqz v3, :cond_14

    move v1, v2

    goto :goto_14

    .line 16
    :pswitch_2c
    sget-object v1, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {v1, v2}, Ljava/io/PrintStream;->println(I)V

    goto :goto_17

    .line 19
    :pswitch_32
    sget-object v1, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const/4 v2, 0x2

    invoke-virtual {v1, v2}, Ljava/io/PrintStream;->println(I)V

    goto :goto_17

    .line 14
    nop

    :sswitch_data_3a
    .sparse-switch
        0x5e918d2 -> :sswitch_18
        0x6c11b92 -> :sswitch_22
    .end sparse-switch

    :pswitch_data_44
    .packed-switch 0x0
        :pswitch_2c
        :pswitch_32
    .end packed-switch
.end method
