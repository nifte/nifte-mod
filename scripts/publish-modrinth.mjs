import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(fileURLToPath(new URL("..", import.meta.url)));
const token = process.env.MODRINTH_TOKEN;
const slug = "nifte-mod";
const api = "https://api.modrinth.com/v2";
const jarPath = resolve(root, "build/libs/nifte-1.0.0.jar");
const iconPath = resolve(root, "src/main/resources/assets/nifte/icon.png");
const body = readFileSync(resolve(root, "README.md"), "utf8");

if (!token) {
	console.error("Set MODRINTH_TOKEN first. Create a token at https://modrinth.com/settings/pats");
	console.error("Required scopes: Create projects, Write projects, Create versions");
	process.exit(1);
}

const headers = {
	Authorization: token,
	UserAgent: "nifte-mod-publish",
};

async function request(method, path, init = {}) {
	const response = await fetch(`${api}${path}`, {
		method,
		headers: {
			...headers,
			...init.headers,
		},
		body: init.body,
	});
	const text = await response.text();
	let json = null;
	if (text) {
		try {
			json = JSON.parse(text);
		} catch {
			json = text;
		}
	}

	if (!response.ok) {
		const detail = typeof json === "string" ? json : JSON.stringify(json, null, 2);
		throw new Error(`${method} ${path} failed (${response.status}): ${detail}`);
	}

	return json;
}

async function createProject() {
	const data = {
		slug,
		title: "Nifte Mod",
		description: "A client-side quality-of-life mod with toggleable HUD, inventory, camera, and crafting features.",
		categories: ["utility"],
		client_side: "required",
		server_side: "unsupported",
		body,
		status: "draft",
		requested_status: "unlisted",
		license_id: "CC0-1.0",
		project_type: "mod",
		is_draft: true,
	};
	const form = new FormData();
	form.append("data", JSON.stringify(data));
	form.append(
		"icon",
		new Blob([readFileSync(iconPath)], { type: "image/png" }),
		"icon.png"
	);
	return request("POST", "/project", { body: form });
}

async function uploadVersion(projectId) {
	const filePart = "nifte-1.0.0.jar";
	const data = {
		name: "Nifte Mod 1.0.0",
		version_number: "1.0.0",
		changelog: "Initial release for Minecraft 26.2.",
		dependencies: [
			{ project_id: "P7dR8mSH", dependency_type: "required" },
			{ project_id: "9s6osm5g", dependency_type: "required" },
			{ project_id: "mOgUt4GM", dependency_type: "optional" },
		],
		game_versions: ["26.2"],
		version_type: "release",
		loaders: ["fabric"],
		featured: true,
		status: "listed",
		project_id: projectId,
		file_parts: [filePart],
		primary_file: filePart,
		environment: "client_only",
	};
	const form = new FormData();
	form.append("data", JSON.stringify(data));
	form.append(
		filePart,
		new Blob([readFileSync(jarPath)], { type: "application/java-archive" }),
		filePart
	);
	return request("POST", "/version", { body: form });
}

async function submitForReview(projectId) {
	return request("PATCH", `/project/${projectId}`, {
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify({ requested_status: "approved" }),
	});
}

const existing = await fetch(`${api}/project/${slug}`, { headers }).then(async (response) => {
	if (response.status === 404) {
		return null;
	}

	if (!response.ok) {
		throw new Error(`Failed to look up project (${response.status}): ${await response.text()}`);
	}

	return response.json();
});

const project = existing ?? await createProject();
console.log(`Project ${project.slug} (${project.id}) status: ${project.status}`);

const versions = existing
	? await request("GET", `/project/${project.id}/version`)
	: [];
const alreadyUploaded = Array.isArray(versions) && versions.some((version) => version.version_number === "1.0.0");
if (alreadyUploaded) {
	console.log("Version 1.0.0 is already on Modrinth.");
} else {
	const version = await uploadVersion(project.id);
	console.log(`Uploaded version ${version.version_number} (${version.id})`);
}

if (project.status === "draft" || project.status === "unlisted") {
	await submitForReview(project.id);
	console.log("Submitted the project for Modrinth review.");
}

console.log(`https://modrinth.com/mod/${project.slug}`);
