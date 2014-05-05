CREATE DATABASE [ReflectionJdbcTestDB];
GO

USE [ReflectionJdbcTestDB]
GO

SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Projects](
	[id] [int] NOT NULL,
	[name] [nchar](200) NOT NULL,
	[date] [datetime] NOT NULL,
	[amount] [decimal](18, 2) NOT NULL,
	[description] [nchar](500) NULL,
	[flag] [bit] NOT NULL,
 CONSTRAINT [PK_Projects] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO


