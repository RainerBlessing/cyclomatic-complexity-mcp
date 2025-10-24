; Simple 6502 assembly example

.proc init
    LDA #$00
    STA $0200
    RTS
.endproc

.proc check_value
    LDA $0200
    BEQ zero_branch
    BNE nonzero_branch
zero_branch:
    LDA #$01
    RTS
nonzero_branch:
    LDA #$02
    RTS
.endproc
