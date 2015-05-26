


package com.kou.utilities;

public class CreateTableSql {


		static String createVolunteerTable()
		{
			String sql="create table if not exists volunteer";
				sql += "( vid int auto_increment primary key,";
				sql += " regid CHAR(120) not null,";
				sql += " last_name CHAR(10) CHARACTER SET utf8 not null,";
				sql += " first_name CHAR(20) CHARACTER SET utf8 not null,";
				sql += " citizen_id CHAR(12) unique not null,";
				sql += " address_street_and_number CHAR(120) CHARACTER SET utf8 not null,";
				sql += " address_city CHAR(10) CHARACTER SET utf8 not null,";
				sql += " mobile_number CHAR(12) ,";
				sql += " team_id CHAR(12) ,";
				sql += " birth_date date, ";
				sql += " rank CHAR(10) default 'new' );";
				return sql;
		}		
		static String createVolunteerNameIndex()
		{
			String sql="create unique index fullname using hash on volunteer";
				sql += "(citizen_id, last_name , first_name);";
				return sql;
		}	
		
		static String createCommitmentTable()
		{
			String sql="create table if not exists commitment";
				sql += "( vid int primary key not null,";
				sql += " citizen_id CHAR(120)  not null,";
				sql += " quantity CHAR(10) not null,";
				sql += " act_type CHAR(10) CHARACTER SET utf8 not null,";//??????????
				sql += " frequency CHAR(10) CHARACTER SET utf8 not null,"; //??????
				sql += " starting_date date default '2014-05-01');";

				return sql;
		}		
		static String createCommitmentIdIndex()
		{
			String sql="create unique index plan_id using hash on commitment";
				sql += "(vid, citizen_id);";
				return sql;
		}
		
		static String createVisitedTable()
		{
			String sql="create table if not exists visited";
				sql += "( vid int primary key not null,";
				sql += " citizen_id CHAR(12) not null,";
				sql += " act_date date  not null default '2014-05-01',";
				sql += " fullname CHAR(20) CHARACTER SET utf8 not null,"; 
				sql += " voter_rating char(10) not null,";		//0-10 scale		
				sql += " mobile_number char(12),";				
				sql += " next_schedule_date date default '2014-05-01');";

				return sql;
		}		
		static String createVisitedIdIndex()
		{
			String sql="create unique index visit_who using hash on visited";
				sql += "(vid, citizen_id, act_date, fullname);";
				return sql;
		}
		
		static String createFundRaisedTable()
		{
			String sql="create table if not exists fund_raised";
				sql += "( vid int primary key not null,";
				sql += " citizen_id CHAR(12) not null,";
				sql += " act_date date not null default '2014-05-01',";
				sql += " fullname CHAR(20) CHARACTER SET utf8 not null,"; 
				sql += " amount char(10) not null,";			
				sql += " mobile_number char(12),";				
				sql += " receipt_number CHAR(12) );";

				return sql;
		}		
		static String createFundRaisedIdIndex()
		{
			String sql="create unique index fund_who using hash on fund_raised";
				sql += "(vid, citizen_id, act_date, fullname);";
				return sql;
		}
		
		static String createAgendaTable()
		{
			String sql="create table if not exists agenda";
				sql += "( aid int auto_increment primary key ,";
				sql += " event_date date  not null default '2014-05-01',";
				sql += " event_time char(10)  not null default '09:00',";
				sql += " title CHAR(60) CHARACTER SET utf8 not null,"; 
				sql += " description text CHARACTER SET utf8 not null,"; 
				//sql += " description2 CHAR(200) CHARACTER SET utf8 not null,"; 
				//sql += " description3 CHAR(200) CHARACTER SET utf8 not null,"; 
				sql += " url CHAR(100) CHARACTER SET utf8 not null,"; 
				sql += " location_street char(100) not null,";			
				sql += " location_city char(20) not null,";			
				sql += " event_host CHAR(200) CHARACTER SET utf8,";				
				sql += " contact_number char(12) );";

				return sql;
		}		
		static String createAgendaDateIndex()
		{
			String sql="create unique index agenda_title using hash on agenda";
				sql += "(act_date, title);";
				return sql;
		}		

		static String createCandidatePhotoTable()
		{
			String sql="create table if not exists candidate_photo";
				sql += " taken_date date not null default '2014-05-01',";
				sql += " title CHAR(60) CHARACTER SET utf8  unique not null,"; 
				sql += " description CHAR(200) CHARACTER SET utf8 not null,"; 
				sql += " url CHAR(100) CHARACTER SET utf8 not null);"; 
				return sql;
		}		
		
		static String createCandidateVocalTable()
		{
			String sql="create table if not exists candidate_vocal";
				sql += " recording_date date not null default '2014-05-01',";
				sql += " title CHAR(60) CHARACTER SET utf8  unique not null,"; 
				sql += " description CHAR(200) CHARACTER SET utf8 not null,"; 
				sql += " url CHAR(100) CHARACTER SET utf8 not null);"; 
				return sql;
		}
		
		static String createCandidateVocalIndex()
		{
			String sql="create unique index voice_title using hash on candidate_vocal";
				sql += "(act_date, title);";
				return sql;
		}
		
		static String createTeamTable()
		{
			String sql="create table if not exists team";
				sql += "( team_id int primary key not null,";
				sql += " mentor_vid int not null,";
				sql += " member_id int unique );";

				return sql;
		}	
		
		static String createTeamIndex()
		{
			String sql="create unique index plan_id using hash on team";
				sql += "(team_id, mentor_vid);";
				return sql;
		}

		static String createBluePrintTable()
		{
			String sql="create table if not exists blue_print";
				sql += "( category CHAR(60) CHARACTER SET utf8 unique not null ,";
				sql += " chapter char(10) not null,";
				sql += " title CHAR(60)  CHARACTER SET utf8 unique not null,"; 
				sql += " section CHAR(10) CHARACTER SET utf8 not null,"; 
				sql += " sub_title CHAR(60)  CHARACTER SET utf8 unique not null,"; 
				sql += " content text CHARACTER SET utf8 not null,"; 
				sql += " url CHAR(100) CHARACTER SET utf8 not null);"; 

				return sql;
		}		
		static String createBluePrintIndex()
		{
			String sql="create unique index plan_id using hash on blue_print";
				sql += "(category, title, sub_title);";
				return sql;
		}				
		
}
