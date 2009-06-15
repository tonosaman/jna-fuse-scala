package org.improving.fuse;

public class Signal 
{
  public static final int	SIGHUP	= 1;	/* hangup */
  public static final int	SIGINT	= 2;	/* interrupt */
  public static final int	SIGQUIT	= 3;	/* quit */
  public static final int	SIGILL	= 4;	/* illegal instruction (not reset when caught) */
  public static final int	SIGTRAP	= 5;	/* trace trap (not reset when caught) */
  public static final int	SIGABRT	= 6;	/* abort() */
  public static final int	SIGPOLL	= 7;	/* pollable event ([XSR] generated, not supported) */
  // public static final int  SIGIOT  = SIGABRT;  /* compatibility */
  // public static final int  SIGEMT  = 7;  /* EMT instruction */
  public static final int	SIGFPE	= 8;	/* floating point exception */
  public static final int	SIGKILL	= 9;	/* kill (cannot be caught or ignored) */
  public static final int	SIGBUS	= 10;	/* bus error */
  public static final int	SIGSEGV	= 11;	/* segmentation violation */
  public static final int	SIGSYS	= 12;	/* bad argument to system call */
  public static final int	SIGPIPE	= 13;	/* write on a pipe with no one to read it */
  public static final int	SIGALRM	= 14;	/* alarm clock */
  public static final int	SIGTERM	= 15;	/* software termination signal from kill */
  public static final int	SIGURG	= 16;	/* urgent condition on IO channel */
  public static final int	SIGSTOP	= 17;	/* sendable stop signal not from tty */
  public static final int	SIGTSTP	= 18;	/* stop signal from tty */
  public static final int	SIGCONT	= 19;	/* continue a stopped process */
  public static final int	SIGCHLD	= 20;	/* to parent on child stop or exit */
  public static final int	SIGTTIN	= 21;	/* to readers pgrp upon background tty read */
  public static final int	SIGTTOU	= 22;	/* like TTIN for output if (tp->t_local&LTOSTOP) */
  public static final int	SIGIO	= 23;	/* input/output possible signal */
  public static final int	SIGXCPU	= 24;	/* exceeded CPU time limit */
  public static final int	SIGXFSZ	= 25;	/* exceeded file size limit */
  public static final int	SIGVTALRM = 26;	/* virtual time alarm */
  public static final int	SIGPROF	= 27;	/* profiling time alarm */
  public static final int SIGWINCH = 28;	/* window size changes */
  public static final int SIGINFO	= 29;	/* information request */
  public static final int SIGUSR1 = 30;	/* user defined signal 1 */
  public static final int SIGUSR2 = 31;	/* user defined signal 2 */
}