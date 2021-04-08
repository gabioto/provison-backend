package pe.telefonica.provision.controller.request;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class KafkaTOARequest {

	private String eventId;
	private String eventTime;
	private String eventType;
	private Event event;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode(callSuper = false)
	public static class Event {
		private Appointment appointment;

		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		@EqualsAndHashCode(callSuper = false)
		public static class Appointment {
			private String id;
			private String href;
			private String creationDate;
			private String description;
			private String type;
			private String timeSlot;
			private String scheduledDate;
			private String scheduler;
			private String startDate;
			private String endDate;
			private String status;
			private String statusReason;
			private String statusChangeDate;
			private String priority;
			private List<ContactMedium> contactMedium = new ArrayList<>();
			private List<Note> note = new ArrayList<>();
			private List<RelatedParty> relatedParty = new ArrayList<>();
			private RelatedPlace relatedPlace;
			private List<RelatedObject> relatedObject = new ArrayList<>();
			private List<AdditionalData> additionalData = new ArrayList<>();

			@Data
			@NoArgsConstructor
			@AllArgsConstructor
			@EqualsAndHashCode(callSuper = false)
			public static class ContactMedium {
				private String aType;
				private String type;
				private String number;
				private String email;
			}

			@Data
			@NoArgsConstructor
			@AllArgsConstructor
			@EqualsAndHashCode(callSuper = false)
			public static class Note {
				private String text;
			}

			@Data
			@NoArgsConstructor
			@AllArgsConstructor
			@EqualsAndHashCode(callSuper = false)
			public static class RelatedParty {
				private String id;
				private String name;
				private String role;
				private List<LegalId> legalId = new ArrayList<>();
				private List<ContactMedium> contactMedium = new ArrayList<>();
				private List<AdditionalData> additionalData = new ArrayList<>();

				@Data
				@NoArgsConstructor
				@AllArgsConstructor
				@EqualsAndHashCode(callSuper = false)
				public static class LegalId {
					private String nationalIdType;
					private String nationalId;
				}

				@Data
				@NoArgsConstructor
				@AllArgsConstructor
				@EqualsAndHashCode(callSuper = false)
				public static class ContactMedium {
					private String type;
					private String number;

				}
			}

			@Data
			@NoArgsConstructor
			@AllArgsConstructor
			@EqualsAndHashCode(callSuper = false)
			public static class RelatedPlace {
				private String id;
				private String name;
				private Address address;
				
				@Data
				@NoArgsConstructor
				@AllArgsConstructor
				@EqualsAndHashCode(callSuper = false)
				public static class Address {
					private String city;
					private String stateOrProvince;
					private String region;
					private String comments;
					private Coordinates coordinates;
					
					@Data
					@NoArgsConstructor
					@AllArgsConstructor
					@EqualsAndHashCode(callSuper = false)
					public static class Coordinates {
						private String longitude;
						private String latitude;

					}
				}
			}

			@Data
			@NoArgsConstructor
			@AllArgsConstructor
			@EqualsAndHashCode(callSuper = false)
			public static class RelatedObject {
				private String involvement;
				private String reference;
				private List<AdditionalData> additionalData = new ArrayList<>();

				@Data
				@NoArgsConstructor
				@AllArgsConstructor
				@EqualsAndHashCode(callSuper = false)
				public static class AdditionalData {
					private String key;
					private String value;

				}
			}

			@Data
			@NoArgsConstructor
			@AllArgsConstructor
			@EqualsAndHashCode(callSuper = false)
			public static class AdditionalData {
				private String key;
				private String value;

			}
		}

	}

}
