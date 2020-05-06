section .text

.globl main
.type main, @function

getMin:
	pushq	%rbp
	movq	%rsp, %rbp
	movl	 %rdi, -24(rbp)
	movl	 %esi, -28(rbp)
	movl	 $0, -4(rbp)
	movl	 $999999, -8(rbp)
	jmp		.L2
.L2:
	movl	 -4(%rbp), %eax
	cmpl	 -28(%rbp), %eax
	jl	.L3
.L3:
	movl	 -4(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	movq	 -24(%rbp), %rax
	addq	%rdx, %rax
	movl	(%rax), %eax
	movl	-24(%rbp), %eax
	cmpl	%eax, -8(%rbp)
	jle	.L4
.L4:
