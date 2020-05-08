.text

getMin:
	pushq	%rbp
	movq	%rsp, %rbp
	movq	 %rdi, -24(%rbp)
	movl	 %esi, -28(%rbp)
	movl	 $0, -4(%rbp)
	movl	 $999999, -8(%rbp)
	jmp		.L2
.L3:
	movl	 -4(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	movq	 -24(%rbp), %rax
	addq	%rdx, %rax
	movl	(%rax), %eax
	cmpl	%eax, -8(%rbp)
	jle	.L4
	movl	-4(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	movq	-24(%rbp), %rax
	addq	%rdx, %rax
	movl	(%rax), %eax
	movl	%eax, -8(%rbp)
.L4:
	addl	$1, -4(%rbp)

.L2:
	movl	 -4(%rbp), %eax
	cmpl	 -28(%rbp), %eax
	jl	.L3
	movl	-8(%rbp), %eax
	popq	%rbp
	ret

.globl main
.type main, @function
main:
	pushq	%rbp
	movq	%rsp, %rbp
	pushq	%r15
	pushq	%r14
	pushq	%r13
	pushq	%r12
	pushq	%rbx
	subq	$40, %rsp
	movq	%rsp, %rax
	movq	%rax, %rbx
	movl	 $3, -52(%rbp)
	movl	 $0, -80(%rbp)
	leaq	-80(%rbp), %rax
	movq	%rax, %rsi
	movl	$.LC0, %edi
	movl	$0, %eax
	call	__isoc99_scanf
	movl	-52(%rbp), %eax
	movslq	%eax, %rdx
	subq	$1, %rdx
	movq	%rdx, -64(%rbp)
	movslq	%eax, %rdx
	movq	%rdx, %r14
	movl	$0, %r15d
	movslq	%eax, %rdx
	movq	%rdx, %r12
	movl	$0, %r13d
	cltq
	leaq	0(,%rax,4), %rdx
	movl	$16, %eax
	subq	$1, %rax
	addq	%rdx, %rax
	movl	$16, %ecx
	movl	$0, %edx
	divq	%rcx
	imulq	$16, %rax, %rax
	subq	%rax, %rsp
	movq	%rsp, %rax
	addq	$3, %rax
	shrq	$2, %rax
	salq	$2, %rax
	movq	%rax, -72(%rbp)
	movq	-72(%rbp), %rax
	movl	$2, (%rax)
	movq	-72(%rbp), %rax
	movl	$0, 4(%rax)
	movq	-72(%rbp), %rax
	movl	$3, 8(%rax)
	movq	-72(%rbp), %rax
	movl	-52(%rbp), %edx
	movl	%edx, %esi
	movq	%rax, %rdi
	call	getMin
	movl	%eax, -76(%rbp)
	movl	-76(%rbp), %eax
	movl	%eax, %esi
	movl	$.LC1, %edi
	movl	$0, %eax
	call	printf
	movl	-80(%rbp), %eax
	movl	%eax, %esi
	movl	$.LC2, %edi
	movl	$0, %eax
	call	printf
	nop
	movq	%rbx, %rsp
	leaq	-40(%rbp), %rsp
	popq	%rbx
	popq	%r12
	popq	%r13
	popq	%r14
	popq	%r15
	popq	%rbp
	ret
.LC0:
	.string "%d"
.LC1:
	.string "%d\n"
.LC2:
	.string "%d\n"
